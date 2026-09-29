## Implementation Plan

Deploying a multi-node Citus cluster on OpenShift requires orchestrating StatefulSets and automating node registration. The optimal approach is an "umbrella" Helm chart that manages separate child charts for the coordinator and worker services.

1. **Initialize the Helm Workspace:** Create an umbrella chart named `citus-cluster` with a `charts/` subdirectory containing `citus-coordinator` and `citus-worker`.
2. **Configure OpenShift Security:** Define a dedicated `ServiceAccount` (`citus-sa`) and bind it to the `anyuid` or `nonroot` Security Context Constraint (SCC) to prevent OpenShift from blocking standard PostgreSQL UID executions.
3. **Deploy the Infrastructure:** Execute `helm install` on the umbrella chart into namespace `postgresql-dev`. The coordinator starts first, followed by the worker StatefulSet (3 worker shards).
4. **Automate Registration:** Use an idempotent registration script (`register.sh`) executed on worker pod startup via `lifecycle.postStart` to run `SELECT * FROM citus_add_node('<worker-hostname>', 5432);` against the coordinator, registering each worker into `pg_dist_node`.

---

## Helm Infrastructure Structure (No `_helpers.tpl`)

By separating the services into their own charts, you can independently scale workers or update coordinator configurations without impacting the entire cluster. All templates utilize direct inline template logic without relying on `_helpers.tpl`.

```text
citus-cluster/                             # Umbrella Chart
├── Chart.yaml                             # Umbrella chart metadata & subchart dependencies
├── values.yaml                            # Global default values (passwords, common labels)
├── values-postgresql-dev.yaml             # Dedicated values profile for 'postgresql-dev' namespace
├── README.md                              # Comprehensive chart documentation
├── openshift/
│   └── scc-setup.sh                       # OpenShift namespace & anyuid SCC setup automation
├── templates/
│   ├── secret.yaml                        # Shared PostgreSQL credential secret
│   ├── serviceaccount.yaml                # Shared ServiceAccount (citus-sa)
│   └── NOTES.txt                          # Deployment guide & verification commands
└── charts/
    ├── citus-coordinator/                 # Coordinator Service Chart (1 Replica)
    │   ├── Chart.yaml
    │   ├── values.yaml
    │   └── templates/
    │       ├── service-client.yaml        # Client ClusterIP service (port 5432)
    │       ├── service-headless.yaml      # Headless service for internal pod DNS
    │       ├── serviceaccount.yaml
    │       └── statefulset.yaml           # Coordinator StatefulSet (1 replica, 10Gi PVC)
    └── citus-worker/                      # Worker Service Chart (3 Shards)
        ├── Chart.yaml
        ├── values.yaml
        └── templates/
            ├── service-headless.yaml      # Headless service (citus-worker-hl)
            ├── configmap-registration-script.yaml # Idempotent auto-registration script
            ├── serviceaccount.yaml
            └── statefulset.yaml           # Worker StatefulSet (3 replicas, 20Gi PVCs)
```

---

## OpenShift Resource Architecture

* **Coordinator Node:** Deployed as a `StatefulSet` with `replicas: 1`. It requires a standard `Service` (`citus-coordinator`) for external application routing and a headless `Service` (`citus-coordinator-hl`) for internal DNS resolution by the workers. It stores metadata but requires minimal persistent storage (10Gi) compared to workers.
* **Worker Nodes:** Deployed as a single `StatefulSet` with `replicas: 3` (3 shards). Using a headless service (`citus-worker-hl`) generates predictable DNS records (e.g., `citus-cluster-citus-worker-0.citus-cluster-citus-worker-hl.postgresql-dev.svc.cluster.local`).
* **Persistent Storage:** Both charts utilize `volumeClaimTemplates` to provision block storage dynamically via OpenShift's default `StorageClass`.
* **Security Contexts:** OpenShift strictly enforces UID ranges. Standard PostgreSQL container images run as UID `999` (`postgres`). The dedicated ServiceAccount (`citus-sa`) is granted the `anyuid` SCC via `oc adm policy add-scc-to-user anyuid -z citus-sa -n postgresql-dev`.

---

## Test & Build Verification

The entire Helm workspace and its individual subcharts have been verified through linting, dry-run template rendering, and packaging.

### 1. Chart Linting Verification

Execute `helm lint` across all subcharts and the umbrella chart:

```bash
# Lint Coordinator Subchart
helm lint ./helm/citus-cluster/charts/citus-coordinator
# Output: 1 chart(s) linted, 0 chart(s) failed

# Lint Worker Subchart
helm lint ./helm/citus-cluster/charts/citus-worker
# Output: 1 chart(s) linted, 0 chart(s) failed

# Lint Umbrella Chart
helm lint ./helm/citus-cluster
# Output: 1 chart(s) linted, 0 chart(s) failed
```

### 2. Standalone Subchart Template Rendering

Each subchart can be rendered independently without errors:

```bash
# Render Coordinator Subchart standalone
helm template test-coord ./helm/citus-cluster/charts/citus-coordinator

# Render Worker Subchart standalone
helm template test-worker ./helm/citus-cluster/charts/citus-worker
```

### 3. Umbrella Template Rendering for `postgresql-dev`

Validate the full cluster manifest generation targeting the `postgresql-dev` namespace:

```bash
helm template citus-cluster ./helm/citus-cluster \
  --namespace postgresql-dev \
  -f ./helm/citus-cluster/values-postgresql-dev.yaml
```

**Rendered Resource Summary:**
- `ServiceAccount`: `citus-sa`
- `Secret`: `citus-cluster-secret` (contains `postgres-password`)
- `ConfigMap`: `citus-cluster-citus-worker-registration-script` (contains `register.sh`)
- `Service`: `citus-cluster-citus-coordinator` (ClusterIP, TCP port 5432)
- `Service`: `citus-cluster-citus-coordinator-hl` (Headless, `clusterIP: None`, `publishNotReadyAddresses: true`)
- `Service`: `citus-cluster-citus-worker-hl` (Headless, `clusterIP: None`, `publishNotReadyAddresses: true`)
- `StatefulSet`: `citus-cluster-citus-coordinator` (`replicas: 1`, 10Gi volume claim template)
- `StatefulSet`: `citus-cluster-citus-worker` (`replicas: 3`, 20Gi volume claim templates)

### 4. Build & Package Verification

Validate chart packaging and archive generation:

```bash
helm package ./helm/citus-cluster/charts/citus-coordinator -d /tmp
# Output: Successfully packaged chart and saved it to: /tmp/citus-coordinator-0.1.0.tgz

helm package ./helm/citus-cluster/charts/citus-worker -d /tmp
# Output: Successfully packaged chart and saved it to: /tmp/citus-worker-0.1.0.tgz

helm package ./helm/citus-cluster -d /tmp
# Output: Successfully packaged chart and saved it to: /tmp/citus-cluster-0.1.0.tgz
```

---

## Ready-to-Run Deployment Commands

### Step 1: OpenShift Namespace & SCC Setup

Run the included automated helper script or use `oc` commands to configure the namespace and grant the `anyuid` SCC to the `citus-sa` ServiceAccount:

```bash
# Using the helper script:
./helm/citus-cluster/openshift/scc-setup.sh postgresql-dev citus-sa

# Or manually:
oc new-project postgresql-dev || oc project postgresql-dev
oc adm policy add-scc-to-user anyuid -z citus-sa -n postgresql-dev
```

### Step 2: Deploy the Citus Cluster via Helm

Deploy the umbrella chart using the dedicated `postgresql-dev` values profile:

```bash
helm install citus-cluster ./helm/citus-cluster \
  --namespace postgresql-dev \
  --create-namespace \
  -f ./helm/citus-cluster/values-postgresql-dev.yaml
```

### Step 3: Monitor Pod Rollout

```bash
oc get pods -n postgresql-dev -w
```

Expected running pods:
```text
NAME                                READY   STATUS    RESTARTS   AGE
citus-cluster-citus-coordinator-0   1/1     Running   0          1m
citus-cluster-citus-worker-0        1/1     Running   0          1m
citus-cluster-citus-worker-1        1/1     Running   0          1m
citus-cluster-citus-worker-2        1/1     Running   0          1m
```

---

## Post-Deployment Verification & Sharding Test

### 1. Confirm Worker Registration in Coordinator

Query `pg_dist_node` on the coordinator to verify that all 3 worker shards registered automatically:

```bash
oc exec -it citus-cluster-citus-coordinator-0 -n postgresql-dev -- \
  psql -U postgres -d postgres -c "SELECT nodeid, nodename, nodeport, isactive FROM pg_dist_node;"
```

Expected output:
```text
 nodeid |                                nodename                                | nodeport | isactive 
--------+------------------------------------------------------------------------+----------+----------
      1 | citus-cluster-citus-worker-0.citus-cluster-citus-worker-hl.postgresql-dev.svc.cluster.local |     5432 | t
      2 | citus-cluster-citus-worker-1.citus-cluster-citus-worker-hl.postgresql-dev.svc.cluster.local |     5432 | t
      3 | citus-cluster-citus-worker-2.citus-cluster-citus-worker-hl.postgresql-dev.svc.cluster.local |     5432 | t
(3 rows)
```

### 2. Test Distributed Table Creation & Sharding

Connect to the coordinator via psql:

```bash
oc exec -it citus-cluster-citus-coordinator-0 -n postgresql-dev -- psql -U postgres
```

Execute distributed sharding SQL commands:

```sql
-- 1. Create standard table
CREATE TABLE customer_events (
    event_id bigserial,
    tenant_id int NOT NULL,
    event_type text NOT NULL,
    payload jsonb,
    PRIMARY KEY (tenant_id, event_id)
);

-- 2. Distribute table across the 3 worker shards:
SELECT create_distributed_table('customer_events', 'tenant_id');

-- 3. Insert records:
INSERT INTO customer_events (tenant_id, event_type, payload)
SELECT (i % 100) + 1, 'click', '{"status":"ok"}'::jsonb FROM generate_series(1, 1000) i;

-- 4. Inspect shard distribution across nodes:
SELECT n.nodename, count(s.shardid) AS shard_count
FROM pg_dist_node n
JOIN citus_shards s ON n.nodename = s.nodename
GROUP BY n.nodename;
```

---

## Debugging & Troubleshooting Mechanism

When operating Citus on OpenShift, localized failures require verifying both Kubernetes orchestration and Citus metadata.

* **Inspect OpenShift Security Constraints:** If pods are stuck in `CreateContainerConfigError`, check the pod events (`oc describe pod <pod-name> -n postgresql-dev`). This typically indicates a missing `anyuid` SCC binding on `citus-sa`.
* **Review Worker Registration Logs:** If a worker does not appear in `pg_dist_node`, check the output of the auto-registration script:
  ```bash
  oc exec -it citus-cluster-citus-worker-0 -n postgresql-dev -- cat /tmp/citus-register.log
  ```
* **Review Worker PostgreSQL Logs:** Use `oc logs statefulset/citus-cluster-citus-worker -c postgresql -n postgresql-dev` to check for replication or connection timeouts indicating network policies blocking port 5432.
* **Test Inter-Node Connectivity:** Use `oc exec -it citus-cluster-citus-coordinator-0 -n postgresql-dev -- pg_isready -h citus-cluster-citus-worker-0.citus-cluster-citus-worker-hl.postgresql-dev.svc.cluster.local` to validate that the coordinator can successfully resolve and ping the worker DNS records.
* **Storage Allocation Issues:** If PVCs remain in `Pending` state, verify OpenShift's default `StorageClass` via `oc get storageclass` or set `global.openshift.storageClass` in `values-postgresql-dev.yaml`.
