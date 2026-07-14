# Aplicações do lab

## app-demo/ — Spring Boot em VM tradicional
Aplicação-alvo rodando via systemd na VM app-java, com log em arquivo
(Logback → `/var/log/app/app.log`, rotação 10MB/7d).

- `pom.xml` + `src/` — código completo (Java 21, Spring Boot 3.4)
- `app-demo.service` — unidade systemd (usuário de serviço dedicado `appsvc`)

**Endpoints de caos** (para exercitar o pipeline):
- `GET /health` — health check
- `GET /api/erro` — exception com stack trace realista (timeout de gateway)
- `GET /api/npe` — NullPointerException real
- `GET /api/pedido/{id}` — WARN se id > 1000
- Batimento agendado a cada 15s gera INFO/WARN contínuos

Build: `mvn package -DskipTests` → `target/app-demo-1.0.0.jar`

## micro-pedidos/ — microserviço em pods (k3s)
Segundo serviço Java, cloud-native: **logs JSON no stdout**
(logstash-logback-encoder) coletados pelo Vector DaemonSet.

- `pom.xml` + `src/` — código completo
- `Dockerfile` — build multi-stage (Maven dentro do container)
- `deploy.yaml` — Deployment (2 réplicas + liveness/readiness probes) +
  Service + Ingress (Traefik)

**Endpoints:**
- `POST /api/pedidos` — ~70% sucesso / 20% WARN (atraso) / 10% ERROR (estoque indisponível)
- `GET /api/crash` — encerra o pod (teste de resiliência: o Kubernetes ressuscita)

Build e deploy no lab (sem registry):
```bash
docker build -t micro-pedidos:1.0 .
docker save micro-pedidos:1.0 -o /tmp/mp.tar
sudo k3s ctr images import /tmp/mp.tar
kubectl apply -f deploy.yaml
```
