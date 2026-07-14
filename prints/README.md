# Evidências (prints)

Em ordem narrativa — do alerta à IA que lembra:

| # | Arquivo | O que mostra |
|---|---|---|
| 01 | `01-zabbix-dashboard.png` | Visão geral do Zabbix: 3 hosts, 408 itens, 224 triggers, ~5,6 valores/s |
| 02 | `02-zabbix-problem-sent.png` | Ciclo do problema (PROBLEM→RESOLVED em 30s) e action log com webhook **Sent** para o n8n |
| 03 | `03-n8n-workflow-triade.png` | Workflow completo com a tríade agentica: Anthropic (raciocínio) + Redis (memória) + HTTP Tool (Loki). Os "30m" de execução são o tempo aguardando aprovação humana — o human-in-the-loop visível |
| 04 | `04-diagnostico-simples.png` | Fase 6: primeiro diagnóstico por IA de um alerta real (timeout de gateway) |
| 05 | `05-diagnostico-triage.png` | Triage cética: a IA identifica o erro "simulado para o lab", **rebaixa a severidade** e sugere suprimir o trigger |
| 06 | `06-diagnostico-falha-honesta.png` | Honestidade epistêmica: com a ferramenta Loki falhando, a IA **declara que não conseguiu investigar** (em vez de inventar dados), diagnostica pelo que tem e entrega a query para verificação manual |
| 07 | `07-diagnostico-investigativo-botoes.png` | Investigação plena: correlação real com os pods do k3s ("estoque-service: connection refused"), conclusão de que o restart não resolve a raiz (`ACAO_PROPOSTA: nenhuma`) e botões de aprovação |
| 08 | `08-diagnostico-recorrente-memoria.png` | **Memória em ação**: "INCIDENTE RECORRENTE", comparação com o diagnóstico anterior, confirmação da hipótese passada e recomendação de escalar High→Critical pela tendência |
| 09 | `09-grafana-explore-loki.png` | Grafana Explore consultando o Loki: stack trace Java agrupado (multiline do Vector) com labels service/host/level |

## Evidências em texto (JSON real das execuções)

- [`10-evidencia-logql-tool.md`](10-evidencia-logql-tool.md) — output real da ferramenta Loki durante uma investigação da IA (os dados citados na correlação)
- [`11-evidencia-remediacao.md`](11-evidencia-remediacao.md) — report da remediação executada após aprovação (+ nota de melhoria do health check)
