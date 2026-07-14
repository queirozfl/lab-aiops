# Arquitetura

## Visão geral

O lab implementa o ciclo completo de gestão inteligente de incidentes:

```
DETECTAR (Zabbix) → ORQUESTRAR (n8n) → INVESTIGAR (AI Agent + Loki) →
DIAGNOSTICAR (Claude) → APROVAR (Telegram) → AGIR (SSH) → CONFIRMAR
```

## Infraestrutura

Host físico: workstation Xeon E3-1240 v3, 32 GB RAM, SSD, Windows + Hyper-V.

| VM | IP | vCPU | RAM | Memória | Papel |
|---|---|---|---|---|---|
| zabbix-srv | 192.168.15.200 | 2 | 4 GB | Dinâmica 2048↔4096 | Detecção |
| app-java | 192.168.15.201 | 2 | 2 GB | Dinâmica 1024↔2048 | App-alvo |
| k3s-srv | 192.168.15.202 | 2 | 4 GB | **Fixa** | Cluster + Loki |
| n8n-srv | 192.168.15.203 | 2 | 2,5 GB | **Fixa** | Orquestração + IA |

**Critério de memória** (aprendido na prática — ver war stories): orquestradores
(Kubernetes, n8n) recebem recursos garantidos; aplicações e serviços comuns podem
ser elásticos, sempre com piso decente e teto definido.

## Separação de responsabilidades

### Detector vs memória investigativa

- **Zabbix é quem acorda**: triggers em tempo real sobre log[] e métricas. Otimizado
  para detecção, não para investigação.
- **Loki é onde se investiga**: todos os logs, de todas as fontes, consultáveis por
  LogQL. É a fonte que o AI Agent usa para contexto.

Essa separação é intencional: cada ferramenta faz o que faz melhor, e o alerta
carrega apenas o gatilho — o contexto é buscado sob demanda.

### Pipeline de logs

```
app.log (Logback, arquivo) ──► Vector (serviço, multiline) ──┐
                                                             ├──► Loki ◄── LogQL (Grafana + AI Agent)
stdout dos pods (JSON) ──────► Vector (DaemonSet, k8s_logs) ─┘
```

Escolhas:
- **Vector** como coletor: agnóstico de destino (troca de Loki para Elasticsearch/
  Datadog mudando só o sink), forte em transformação (VRL), padrão crescente de mercado.
- **Multiline** no source de arquivo: stack traces Java viram uma entrada única —
  essencial para a análise por IA.
- **Logs JSON no k8s**: o micro-pedidos loga JSON no stdout (logstash-logback-encoder);
  o Vector extrai o `level` no parse.

## O agente de IA

Nó **AI Agent** do n8n com:
- **Chat Model**: Claude (Anthropic API)
- **Tool**: HTTP Request para `GET /loki/api/v1/query_range`, com o parâmetro `query`
  definido pelo modelo ($fromAI) — a IA escreve o próprio LogQL
- **Prompt**: processo obrigatório de investigação (logs do serviço afetado +
  correlação com outros serviços) antes do diagnóstico; saída estruturada com
  `ACAO_PROPOSTA` na última linha

## Fluxo de remediação

1. IF sobre a saída do Agent: só oferece aprovação se `ACAO_PROPOSTA: restart_app`
2. Telegram **Send and Wait for Response** (Approval) — botões nativos
3. IF sobre `data.approved`
4. Nó SSH com credencial dedicada (chave `automacao@n8n`)
5. Comando com verificação: `restart && sleep 15 && is-active && curl /health`
6. Telegram com o resultado

Segurança da execução: ver [05-seguranca.md](05-seguranca.md).

## Memória do agente

Redis Chat Memory plugada no conector Memory do AI Agent:

- **Session key = hostname** do alerta → o agente "lembra" por host
- Janela de 5 interações; TTL 0 (ajustável p/ ex. 86400 = memória de 24h)
- Redis 7 alpine no compose da n8n-srv, sem porta exposta ao host

Ciclo por execução: `loadMemoryVariables` (antes de raciocinar) → diagnóstico →
`saveContext`. A tríade agentica completa: **raciocínio** (LLM) +
**ferramentas** (Loki/LogQL) + **memória** (Redis por host).
