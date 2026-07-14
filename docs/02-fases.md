# Fases de implementação

Roteiro executado, na ordem. Cada fase terminava com validação funcional e
checkpoint da VM no Hyper-V.

## Fase 1 — Fundação
- VM zabbix-srv no Hyper-V (Gen 2, Secure Boot "Microsoft UEFI CA")
- Ubuntu Server 26.04, OpenSSH, usuário padrão `ops`
- IP estático via netplan (cloud-init de rede desabilitado)

## Fase 2 — Zabbix
- Zabbix 7.4 + MySQL + Nginx (repo oficial p/ Ubuntu 26.04)
- Schema importado, frontend na porta 8080, timezone America/Sao_Paulo
- Agente local, host "Zabbix server" ZBX verde

## Fase 3 — Aplicação Java + monitoramento de log
- VM app-java, Java 21, Maven
- App Spring Boot com endpoints de caos: `/api/erro` (exception c/ stack trace),
  `/api/npe`, `/api/pedido/{id}` (WARN condicional), batimento agendado
- Logback → `/var/log/app/app.log` com rotação
- systemd unit com usuário de serviço dedicado (`appsvc`)
- Zabbix Agent 2: item `log[...,"(ERROR|WARN)",,,skip]` **active**, tipo Log,
  1s, history 7d
- Trigger: dispara em ERROR (WARN é contexto, não alarme)

## Fase 4 — Kubernetes
- VM k3s-srv, k3s single-node (binário único, CNCF-certified)
- micro-pedidos: segundo serviço Java, logs JSON no stdout
- Dockerfile multi-stage (build Maven dentro do container)
- Imagem importada via `k3s ctr images import` (lab sem registry)
- Deployment 2 réplicas + probes + Service + Ingress (Traefik)
- Monitoração dupla: Zabbix Agent 2 (VM) + template "Kubernetes cluster state
  by HTTP" (ServiceAccount + ClusterRole view + token)

## Fase 5 — n8n
- VM n8n-srv, Docker + Compose, n8n com volume persistente
- Bot Telegram (BotFather) + chat_id
- Workflow de teste: Webhook → Telegram (lição: Fixed vs Expression)

## Fase 6 — Inteligência
- Chave Anthropic (expiração 30d) + créditos
- Media type Webhook no Zabbix: parâmetros com macros + script JS (HttpRequest)
- Message templates Problem/Recovery (obrigatórios mesmo p/ webhook)
- Media no usuário + Trigger action com operação em **Operations** (não Recovery!)
- Workflow: Webhook → AI Agent (Claude) → Telegram
- Primeiro diagnóstico por IA de um alerta real

## Fase 6.5 — Observabilidade de logs
- Loki no k3s (namespace observability, NodePort 31000)
- Vector na app-java (source file + multiline p/ stack traces)
- Vector DaemonSet no k3s (kubernetes_logs + RBAC + parse do JSON)
- Grafana na n8n-srv (datasource Loki, Explore)
- **Upgrade do agente**: HTTP Request Tool com query LogQL definida pelo modelo;
  prompt com processo investigativo obrigatório e correlação

## Fase 7 — Auto-remediação
- Chave SSH dedicada `automacao@n8n`
- sudoers com allowlist exata de comandos (restart/status do app-demo)
- Telegram "Send and Wait for Response" (Approval) → IF → SSH → report
- Gate de proposta: só oferece botões quando `ACAO_PROPOSTA != nenhuma`
- Validação pós-remediação: is-active + health check HTTP

## Evolução pós-Fase 7 — Memória do agente
- Redis no compose da n8n-srv (sem porta exposta)
- Redis Chat Memory no AI Agent: Session ID "Define below" com key por host
- Prompt: instrução para considerar histórico e identificar recorrência
- Validação: 2º alerta do mesmo host reconhecido como "INCIDENTE RECORRENTE",
  com comparação de evidências entre ciclos e recomendação de escalar severidade
