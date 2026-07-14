# Como reproduzir (visão geral)

Pré-requisitos: hypervisor (Hyper-V ou equivalente) com ~13 GB livres, ISO do
Ubuntu Server, conta Telegram, chave de API de um LLM (Anthropic).

Ordem recomendada (detalhes em 02-fases.md):

1. **zabbix-srv (.200)**: Ubuntu → Zabbix 7.4 + MySQL + Nginx → IP estático
2. **app-java (.201)**: Java 21 → app-demo (apps/app-demo) → systemd →
   Zabbix Agent 2 → item log[] + trigger
3. **k3s-srv (.202)**: k3s → build do micro-pedidos (Docker) → import no
   containerd → deploy → Agent 2 + template Kubernetes (ServiceAccount)
4. **n8n-srv (.203)**: Docker Compose (n8n/docker-compose.yml) → bot Telegram
5. **Integração**: media type webhook (zabbix/) → workflow (n8n/) → chave LLM
6. **Logs**: Loki (k8s/) → Vector na VM (vector/) e DaemonSet (k8s/) → Grafana
7. **Remediação**: chave SSH de automação → sudoers (zabbix/sudoers-...) →
   Send-and-Wait + IF + SSH no workflow

Liturgia pós-instalação de cada VM: IP estático (netplan, cloud-init de rede
off), `apt upgrade`, expandir LVM, chave SSH, timezone, checkpoint.

Testes de fogo:
- `curl http://<app-java>:8080/api/erro` → diagnóstico investigativo no Telegram
- `curl -X POST http://<k3s>/api/pedidos` (loop) → tráfego e WARNs nos pods
- `curl http://<k3s>/api/crash` → pod morre e o Kubernetes ressuscita
