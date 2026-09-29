# Citus Distributed PostgreSQL Helm Chart for OpenShift

This Helm umbrella chart deploys a production-ready, multi-node Citus Distributed PostgreSQL cluster on Red Hat OpenShift.

## Architecture Overview

```
                                  +-----------------------------+
                                  |   Client Applications       |
                                  +--------------+--------------+
                                                 |
                                                 v
                                  +-----------------------------+
                                  |  citus-coordinator (Svc)   |
                                  |          Port 5432          |
                                  +--------------+--------------+
                                                 |
                                                 v
                                  +-----------------------------+
                                  |      citus-coordinator      |
                                  |     (StatefulSet, rep: 1)   |
                                  +-------+--------------+------+
                                          |              |
                      +-------------------+              +-------------------+
                      |                                                      |
                      v                                                      v
        +---------------------------+                          +---------------------------+
        |      citus-worker-0       |                          |      citus-worker-1       |
        |  (Shard 1, StatefulSet)   |   ...                    |  (Shard 2, StatefulSet)   |
        +---------------------------+                          +---------------------------+
```

- **Namespace:** `postgresql-dev`
- **Coordinator Node:** 1 instance (`StatefulSet` with `replicas: 1`) storing distributed metadata, query planner, and executor.
- **Worker Nodes:** 3 instances (`StatefulSet` with `replicas: 3`) storing table shards and executing parallel shard queries.
- **Auto-Registration:** Workers automatically register with the coordinator upon startup via `SELECT * FROM citus_add_node(...)` configured inside `configmap-registration-script.yaml`.

---

## Directory Structure

```text
citus-cluster/                             # Umbrella Chart
├── Chart.yaml                             # Umbrella chart metadata & subchart dependencies
├── values.yaml                            # Global default values
├── values-postgresql-dev.yaml             # Dedicated values profile for 'postgresql-dev'
├── openshift/
│   └── scc-setup.sh                       # OpenShift SCC and project setup script
├── templates/
│   ├── _helpers.tpl                       # Chart helper template functions
│   ├── secret.yaml                        # Shared PostgreSQL credential secret
│   ├── serviceaccount.yaml                # Dedicated ServiceAccount (citus-sa)
│   └── NOTES.txt                          # Post-install guide & verification instructions
└── charts/
    ├── citus-coordinator/                 # Coordinator Subchart
    │   ├── Chart.yaml
    │   ├── values.yaml
    │   └── templates/
    │       ├── _helpers.tpl
    │       ├── service-client.yaml        # Client ClusterIP service for app connections
    │       ├── service-headless.yaml      # Headless service for internal pod DNS
    │       ├── serviceaccount.yaml
    │       └── statefulset.yaml           # Coordinator StatefulSet (1 replica)
    └── citus-worker/                      # Worker Subchart
        ├── Chart.yaml
        ├── values.yaml
        └── templates/
            ├── _helpers.tpl
            ├── service-headless.yaml      # Headless service for predictable shard DNS
            ├── configmap-registration-script.yaml # Auto-registration script
            ├── serviceaccount.yaml
            └── statefulset.yaml           # Worker StatefulSet (3 replicas)
```

---

## Prerequisites & OpenShift Security Configuration

OpenShift enforces strict Security Context Constraints (SCC). Standard PostgreSQL container images run as UID `999` (`postgres`). To allow this without permission errors:

### 1. Grant `anyuid` SCC to `citus-sa`

Run the included automated setup script (requires OpenShift cluster admin or namespace admin privileges):

```bash
# Using the helper script:
./openshift/scc-setup.sh postgresql-dev citus-sa

# Or manually via the OpenShift CLI:
oc new-project postgresql-dev || oc project postgresql-dev
oc adm policy add-scc-to-user anyuid -z citus-sa -n postgresql-dev
```

---

## Deployment to OpenShift

### Step 1: Install the Chart

Install into the `postgresql-dev` namespace using the preconfigured `citus-cluster/values-postgresql-dev.yaml`:

```bash
helm install citus-cluster ./helm/citus-cluster \
  --namespace postgresql-dev \
  --create-namespace \
  -f ./helm/citus-cluster/values-postgresql-dev.yaml
```

### Step 2: Monitor Pod Rollout

```bash
oc get pods -n postgresql-dev -w
```

Expected output:
```text
NAME                                  READY   STATUS    RESTARTS   AGE
citus-cluster-citus-coordinator-0     1/1     Running   0          1m
citus-cluster-citus-worker-0          1/1     Running   0          1m
citus-cluster-citus-worker-1          1/1     Running   0          1m
citus-cluster-citus-worker-2          1/1     Running   0          1m
```

---

## Verification & Validation

### 1. Verify Citus Worker Shard Registration

Check `pg_dist_node` on the coordinator to verify all 3 workers registered automatically:

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

### 2. Test Sharded Distributed Table Creation

Connect to the coordinator and test distributed table sharding across the 3 worker nodes:

```bash
oc exec -it citus-cluster-citus-coordinator-0 -n postgresql-dev -- psql -U postgres
```

Inside the PostgreSQL prompt:

```sql
-- 1. Create standard table
CREATE TABLE customer_events (
    event_id bigserial,
    tenant_id int NOT NULL,
    event_type text NOT NULL,
    payload jsonb,
    created_at timestamptz DEFAULT now(),
    PRIMARY KEY (tenant_id, event_id)
);

-- 2. Distribute the table across worker nodes by tenant_id
SELECT create_distributed_table('customer_events', 'tenant_id');

-- 3. Insert sample rows across different tenant IDs
INSERT INTO customer_events (tenant_id, event_type, payload)
SELECT 
    (i % 100) + 1,
    'login_event',
    json_build_object('user_idx', i, 'status', 'success')::jsonb
FROM generate_series(1, 1000) i;

-- 4. Verify shard distribution across worker nodes
SELECT 
    n.nodename,
    count(s.shardid) AS shard_count
FROM pg_dist_node n
JOIN citus_shards s ON n.nodename = s.nodename
GROUP BY n.nodename
ORDER BY n.nodename;
```

---

## Scaling Workers

To scale the worker pool (e.g. from 3 to 5 shards):

```bash
helm upgrade citus-cluster ./helm/citus-cluster \
  --namespace postgresql-dev \
  --set citus-worker.replicaCount=5 \
  --reuse-values
```

Newly spawned worker pods (`citus-worker-3`, `citus-worker-4`) will execute the postStart auto-registration script and join `pg_dist_node` automatically.

---

## Troubleshooting Guide

| Issue | Cause | Solution |
| :--- | :--- | :--- |
| `CreateContainerConfigError` or `CrashLoopBackOff` on OpenShift | Namespace SCC preventing UID 999 execution | Run `oc adm policy add-scc-to-user anyuid -z citus-sa -n postgresql-dev` |
| Worker not showing in `pg_dist_node` | PostStart registration script failed or timed out | Check logs: `oc exec citus-cluster-citus-worker-0 -n postgresql-dev -- cat /tmp/citus-register.log` |
| Coordinator cannot resolve worker DNS | Headless service misconfigured | Run test from coordinator: `oc exec -it citus-cluster-citus-coordinator-0 -n postgresql-dev -- pg_isready -h citus-cluster-citus-worker-0.citus-cluster-citus-worker-hl.postgresql-dev.svc.cluster.local` |
| Volume provisioning stuck in `Pending` | StorageClass does not support dynamic volume provisioning | Specify cluster StorageClass in `citus-cluster/values-postgresql-dev.yaml` under `citus-coordinator.persistence.storageClass` and `citus-worker.persistence.storageClass` |
