# Manifests Kubernetes (k3s)

Manifests aplicados no cluster (namespace `observability`):

- **`loki.yaml`** — Namespace + ConfigMap (schema tsdb v13, retenção 7d) +
  Deployment (limits 512Mi) + Service **NodePort 31000** (endpoint consultado
  pelo Grafana e pela ferramenta do AI Agent)
- **`vector-k8s.yaml`** — ServiceAccount + ClusterRole (list/watch de
  pods/namespaces/nodes) + ConfigMap (source `kubernetes_logs`, parse do JSON
  dos pods extraindo `level`) + DaemonSet enviando ao Loki via service interno

O deploy do microserviço (`deploy.yaml`) acompanha os fontes em
[`apps/micro-pedidos/`](../apps/micro-pedidos/).

Aplicação:
```bash
kubectl apply -f loki.yaml
kubectl apply -f vector-k8s.yaml
kubectl get pods -n observability   # loki Running + vector Running
curl http://<ip-do-node>:31000/ready
```

Notas de lab: Loki com `emptyDir` (logs não sobrevivem à recriação do pod —
produção usaria PVC) e `auth_enabled: false` (rede isolada).
