# n8n — orquestração e o agente de IA

## Arquivos

- **`docker-compose.yml`** — n8n + Grafana + Redis (memória do agente),
  com volumes persistentes. Redis sem porta exposta (rede interna).
- **`zabbix-aiops.workflow.json`** — o workflow completo, **sanitizado e
  pronto para importar** (credenciais e chat ID substituídos por placeholders).
- **`prompt-ai-agent.md`** — o prompt do agente e a configuração da
  ferramenta Loki e da memória Redis, documentados.

## Importando o workflow

1. No n8n: **Workflows → ⋯ → Import from File** → `zabbix-aiops.workflow.json`
2. Configure as credenciais nos nós marcados `CONFIGURE_NO_SEU_N8N`:
   - **Anthropic** (Chat Model) — chave `sk-ant-...`
   - **Telegram** (3 nós) — token do bot + substitua `SEU_CHAT_ID`
   - **SSH** (Execute a command) — chave privada da automação
   - **Redis** (Chat Memory) — host `redis`, porta 6379
3. Ajuste os IPs se o seu lab usar outra faixa (Loki na ferramenta do agente,
   host SSH)
4. Ative o workflow (importa inativo por segurança)

## Estrutura

```
Webhook → AI Agent (Claude + Tool Loki + Memória Redis)
        → Telegram Send-and-Wait (Approval)
        → IF (approved) → SSH (restart c/ verificação) → Telegram resultado
                        ↘ Telegram rejeição
```
