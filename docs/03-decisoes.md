# Decisões de arquitetura (ADRs)

Registro das decisões relevantes e seus trade-offs.

## ADR-001 — Hyper-V como hypervisor
**Contexto:** workstation Windows já disponível, 32 GB RAM.
**Decisão:** usar Hyper-V em vez de Proxmox/nuvem.
**Trade-off:** gratuito, checkpoints, hardware ocioso vs. não é ambiente de
produção típico. Adequado para lab.

## ADR-002 — k3s em vez de k8s completo / minikube / kind
**Decisão:** k3s single-node.
**Racional:** Kubernetes real e CNCF-certified num binário de ~70 MB; mesmos
manifests/kubectl de produção; comporta-se como cluster "de verdade" numa VM só.

## ADR-003 — Spring Boot em vez de JBoss/WildFly
**Racional:** sobe com 512 MB, log configurável em minutos, endpoints de caos
fáceis. JBoss adicionaria consumo e complexidade sem ganho para o objetivo.

## ADR-004 — Vector em vez de Promtail/Alloy
**Contexto:** Promtail descontinuado; Alloy é o par nativo do Loki.
**Decisão:** Vector.
**Racional:** presença de mercado, agnóstico de destino (sink trocável),
transformação poderosa (VRL), cobre os dois cenários (arquivo e kubernetes_logs)
com uma ferramenta.

## ADR-005 — Detector separado da memória investigativa
**Decisão:** Zabbix continua sendo o gatilho (log[] com trigger); Loki é onde a
IA investiga.
**Racional:** cada ferramenta no seu forte. O alerta transporta o mínimo; o
contexto é buscado sob demanda — reduz payload, aumenta a riqueza da análise.

## ADR-006 — Memória fixa para orquestradores, dinâmica para o resto
**Contexto:** kernel panic por OOM na VM do k3s com memória dinâmica
(RAM mínima de 512 MB permitiu balão encolher a VM sob pressão).
**Decisão:** k3s-srv e n8n-srv com RAM fixa; zabbix-srv e app-java dinâmicas
com piso ≥ 50% e teto = valor planejado (nunca o default de 1 TB).
**Racional:** Kubernetes inventaria a RAM total para agendar; balão quebra essa
premissa. Mesma lógica de requests/limits, uma camada abaixo.

## ADR-007 — Human-in-the-loop obrigatório na remediação
**Decisão:** nenhuma ação executa sem aprovação explícita (botões no Telegram).
**Racional:** confiança em automação se constrói gradualmente; o gate humano
permite auditar as propostas da IA antes de conceder autonomia. Evolução
futura: auto-aprovação para ações de baixo risco com bom histórico.

## ADR-008 — Least privilege na execução
**Decisão:** chave SSH exclusiva da automação + sudoers com allowlist de
comandos exatos (restart/status de UM serviço).
**Racional:** blast radius mínimo — o comprometimento da chave permite, no
máximo, reiniciar o app-demo. Argumentos importam no sudoers
(`systemctl status app-demo *` ≠ `systemctl status app-demo`).

## ADR-009 — LLM via API (Claude) em vez de Ollama local
**Racional:** hardware sem GPU tornaria cada diagnóstico lento demais para
estudo iterativo; custo por diagnóstico é de centavos. O "cérebro" é plugável —
o nó Chat Model é substituível sem tocar no resto do workflow.

## ADR-010 — A IA propõe, com vocabulário fechado
**Decisão:** o prompt exige `ACAO_PROPOSTA: <restart_app | nenhuma>` e o fluxo
só oferece aprovação quando há ação válida.
**Racional:** ações executáveis vêm de um catálogo controlado — a IA escolhe
DENTRO dele, nunca inventa comandos. Extensão futura = adicionar entradas ao
catálogo (limpar disco, rollout restart), cada uma com seu sudoers.

## ADR-011 — Memória de sessão por host (Redis Chat Memory)
**Contexto:** cada alerta era diagnosticado do zero; incidentes recorrentes não
eram reconhecidos como tal.
**Decisão:** Redis Chat Memory no AI Agent com **session key = hostname**
(`{{ $('Webhook').item.json.body.host }}`), janela de 5 interações, Redis em
container sem porta exposta (rede interna do compose).
**Resultado observado:** no segundo alerta do mesmo host, o agente declarou
"incidente recorrente", comparou com o diagnóstico anterior, confirmou a
hipótese passada com evidência nova e recomendou escalar a severidade com base
na tendência.
**Trade-offs:** custo de tokens cresce (histórico injetado em cada chamada);
janela pequena limita a "memória de runbook" de longo prazo — para busca
semântica em todo o histórico, o caminho é Vector Store como Tool (roadmap).
