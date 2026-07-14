# Troubleshooting — war stories

Incidentes reais do lab, com diagnóstico e lição. (Os melhores aprendizados
saíram daqui.)

## 1. Kernel panic na VM do Kubernetes

**Sintoma:** kernel panic durante a instalação do Docker na k3s-srv; após
reboot, `journalctl -b -1` mostrou `Out of memory: Killed process (traefik)`.

**Causa raiz:** memória dinâmica do Hyper-V com RAM mínima padrão (512 MB).
Sob pressão do host, o balão encolheu a VM no exato momento de pico
(k3s + Traefik + apt). O OOM killer massacrou processos até o pânico.

**Correção:** RAM fixa de 4096 MB para a VM do k3s. Critério generalizado no
ADR-006.

**Lição:** hypervisor com balão + orquestrador de containers = OOM esperando
para acontecer. "RAM mínima" é o valor com que a VM pode acabar operando.

## 2. LVM pela metade

**Sintoma:** disco de 30 GB, mas `df -h /` mostrando 14 GB (54% usados).

**Causa:** comportamento padrão do instalador do Ubuntu com LVM — o volume
lógico da raiz é criado com ~metade do volume group, deixando o resto reservado.

**Correção (online, sem reboot):**
```bash
sudo lvextend -l +100%FREE /dev/ubuntu-vg/ubuntu-lv
sudo resize2fs /dev/ubuntu-vg/ubuntu-lv
```

**Lição:** virou item da liturgia pós-instalação de toda VM.

## 3. O pipeline de alertas do Zabbix, camada por camada

Série de falhas ao ativar o webhook para o n8n — cada erro ensinou uma camada:

| Erro | Camada | Causa |
|---|---|---|
| Nenhum envio no PROBLEM | Action/Operations | Operação criada em **Recovery operations** em vez de **Operations** |
| "No media defined for user" | Media do usuário | Update do popup salvo, Update do formulário do usuário esquecido (+ cache do server) |
| "No message defined for media type" | Message template | Webhook também exige template Problem/Recovery, mesmo sem usar o corpo |
| "Could not resolve host: undefined" | Script | Parâmetro nomeado `URL`, script lendo `params.url` — case-sensitive |

**Lição:** o caminho completo é `trigger → event → action (Operations!) → media
do usuário → message template → script`. Sabendo as camadas, qualquer "Failed"
no Action log se resolve em minutos.

## 4. Agente com hostname padrão

**Sintoma:** host k3s-srv sem dados, problema "agent is not available" por horas,
mas o serviço ativo.

**Diagnóstico:** `journalctl` do agente mostrou `hostname: [Zabbix server]` —
a configuração (sed) nunca foi aplicada; o agente se anunciava com o nome default
e o servidor esperava `k3s-srv`.

**Lição:** o par (Hostname no conf ↔ Host name no frontend) precisa ser idêntico;
o log de inicialização do agente mostra o hostname efetivo.

## 5. A IA encontrou um bug de timezone

**Evento:** num diagnóstico, a IA apontou espontaneamente: "Diferença de 3h entre
horário do log e do alerta. Verificar sincronização de timezone."

**Confirmação:** a JVM da app-java rodava em UTC; o Zabbix em America/Sao_Paulo.

**Correção:** `timedatectl set-timezone America/Sao_Paulo` + restart do app.

**Lição:** valor não solicitado de AIOps — a correlação de timestamps entre
fontes revelou inconsistência de configuração que passaria despercebida.

## 6. sudoers e a exatidão dos argumentos

**Sintoma:** `sudo: A terminal is required` via SSH não-interativo, mesmo com
NOPASSWD configurado.

**Causa:** sudoers autorizava `systemctl status app-demo`; o comando executado
era `systemctl status app-demo --no-pager` — argumentos diferentes, regra não
casa, cai na regra geral (senha).

**Correção:** `systemctl status app-demo *` para flags; restart mantido exato.

**Lição:** a rigidez que atrapalhou é a mesma que protege — restart autorizado
de um serviço não autoriza restart de outro.

## 7. Colagens truncadas (recorrente)

Três episódios: App.java vazio (build "Unable to find main class"), pasta do
micro-pedidos sem os fontes, docker-compose com serviço dentro de `volumes:`
(via `cat >>` em posição errada).

**Lição-padrão:** validar após colar/editar — `wc -l`, `find`,
`docker compose config --quiet`, `sudo visudo -c`, `vector validate`. Confiar,
mas conferir.
