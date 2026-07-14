# Segurança

Medidas aplicadas no lab (proporcionais a um ambiente de estudo) e o racional.

## Identidades e acesso

- **Usuário humano padronizado** (`ops`) em todas as VMs; contas de serviço
  separadas (`zabbix`, `appsvc`, `vector`) sem login/sudo
- **Chaves SSH por função**, topologia estrela:
  - chave pessoal → administração (todas as VMs)
  - chave `automacao@n8n` → somente alvos de remediação
- VMs não se acessam entre si (sem malha de chaves)

## Least privilege na automação

`/etc/sudoers.d/n8n-automation` na app-java:

```
ops ALL=(root) NOPASSWD: /usr/bin/systemctl restart app-demo, /usr/bin/systemctl status app-demo *, /usr/bin/systemctl is-active app-demo
```

- Allowlist de comandos **exatos** — argumentos importam
- Blast radius de um vazamento da chave: reiniciar um serviço específico
- Validação de sintaxe obrigatória: `sudo visudo -c`

## Credenciais e tokens

| Credencial | Escopo | Expiração/rotação |
|---|---|---|
| Chave API Anthropic | Workspace do lab | 30 dias |
| Token API Zabbix | Consulta (usado pela IA) | definida na criação |
| Token bot Telegram | Bot do lab | revogável via BotFather |
| Token ServiceAccount k8s | ClusterRole `view` (read-only) | secret dedicado |

Credenciais armazenadas no credential store do n8n (não em texto plano nos
workflows).

## Human-in-the-loop

Nenhuma ação de remediação executa sem aprovação explícita via Telegram.
A IA propõe dentro de um vocabulário fechado (`ACAO_PROPOSTA`), nunca comandos
livres.

## RBAC no Kubernetes

- ServiceAccount `zabbix-monitor`: ClusterRole `view` (somente leitura)
- ServiceAccount `vector`: ClusterRole restrito a list/watch de pods/namespaces/nodes

## Limitações conhecidas (lab)

- Sem TLS interno (HTTP entre componentes na LAN)
- Frontends com autenticação local simples
- Loki sem autenticação (auth_enabled: false) — aceitável em rede de lab isolada
- Logs podem conter dados sensíveis em produção → mascaramento no Vector (VRL)
  seria o próximo passo
