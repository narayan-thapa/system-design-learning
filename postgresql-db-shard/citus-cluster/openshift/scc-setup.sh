#!/usr/bin/env bash
set -euo pipefail

# Script to configure OpenShift namespace and SCC for Citus Cluster
NAMESPACE="${1:-postgresql-dev}"
SERVICE_ACCOUNT="${2:-citus-sa}"

echo ">>> Setting up OpenShift environment for Citus Distributed PostgreSQL..."
echo ">>> Namespace: ${NAMESPACE}"
echo ">>> ServiceAccount: ${SERVICE_ACCOUNT}"

# 1. Create namespace if not exists
if ! oc get project "${NAMESPACE}" >/dev/null 2>&1; then
  echo ">>> Creating OpenShift project/namespace: ${NAMESPACE}"
  oc new-project "${NAMESPACE}" || oc create namespace "${NAMESPACE}"
else
  echo ">>> Namespace ${NAMESPACE} already exists."
fi

# 2. Grant 'anyuid' SCC to the dedicated ServiceAccount
echo ">>> Granting 'anyuid' Security Context Constraint (SCC) to ServiceAccount '${SERVICE_ACCOUNT}' in namespace '${NAMESPACE}'..."
oc adm policy add-scc-to-user anyuid -z "${SERVICE_ACCOUNT}" -n "${NAMESPACE}"

echo ">>> OpenShift SCC setup completed successfully!"
echo ">>> You can now deploy the Helm chart with:"
echo "    helm install citus-cluster ./helm/citus-cluster -n ${NAMESPACE} -f ./helm/citus-cluster/values-postgresql-dev.yaml"
