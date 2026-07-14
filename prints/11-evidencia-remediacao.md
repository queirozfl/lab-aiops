# Evidência 11 — Remediação executada após aprovação humana

Resposta da API do Telegram à mensagem de report enviada pelo n8n **após** a
aprovação (botão ✅) e a execução do restart via SSH:

```json
{
  "ok": true,
  "result": {
    "message_id": 28,
    "from": {"is_bot": true, "first_name": "Lab bot"},
    "chat": {"type": "private"},
    "text": "🦾 REMEDIAÇÃO EXECUTADA em app-java\nComando: restart do app-demo\nResultado: active\n health=000\n\nThis message was sent automatically with n8n"
  }
}
```

O ciclo completo: diagnóstico → proposta → aprovação → execução SSH (sudoers
com allowlist) → verificação → report.

Nota de melhoria registrada: o `health=000` indica que o health check HTTP
rodou antes do Spring Boot abrir a porta (~10-15s de boot). Correção aplicada
no comando do nó SSH: `sleep 3` → `sleep 15` (em produção: retry loop com
timeout). O `systemctl is-active` já confirmava o serviço `active`.
