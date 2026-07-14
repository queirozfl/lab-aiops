# Roadmap — próximas evoluções

## Fase 8 — Infra como código na nuvem
Migrar as 4 VMs para cloud com **Terraform** (rede/VPC, security groups por
função, instâncias) + **cloud-init/Ansible** substituindo a liturgia manual.
Candidatos: Oracle Cloud Always Free (custo zero, ARM), Hetzner (custo-benefício
x86), AWS (valor de mercado; usar destroy/apply para controlar custo).
Cuidados: exposição pública (subnet privada + VPN/Tailscale), secrets manager.

## Fase 9 — Agente que aprende
- **Vector Store de runbooks**: gravar cada diagnóstico num vetor store
  (Qdrant/PGVector) e plugar como Tool de busca semântica — "já vimos esse
  padrão de erro?" em todo o histórico, além da janela do Redis.
- **Avaliar Hermes Agent / RunbookHermes** (Nous Research): agente autônomo
  self-hosted com loop de aprendizado (skills a partir de incidentes
  resolvidos). Experimento: rodar em paralelo ao pipeline n8n nos mesmos
  incidentes e comparar diagnósticos — orquestração controlada vs agente
  autônomo com memória evolutiva.

## Melhorias pontuais
- Recovery operations com resumo pela IA ("problema resolvido, causa foi...")
- Catálogo de remediações ampliado (limpar disco, kubectl rollout restart),
  cada ação com seu sudoers/RBAC
- TTL na memória (ex.: 24h) para esquecimento natural de incidentes antigos
- Mascaramento de dados sensíveis nos logs (Vector VRL)
- TLS interno e autenticação no Loki
