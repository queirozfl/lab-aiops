# Evidência 10 — A ferramenta de investigação em ação (HTTP Request Tool → Loki)

Output real do tool durante uma execução do AI Agent (aba Executions do n8n).
A IA consultou o Loki por conta própria e recebeu os logs do micro-pedidos no
Kubernetes — os mesmos dados que citou na seção CORRELAÇÃO do diagnóstico:

```json
{
  "status": "success",
  "data": {
    "resultType": "streams",
    "result": [{
      "stream": {
        "cluster": "k3s-lab",
        "level": "warn",
        "namespace": "default",
        "service": "micro-pedidos"
      },
      "values": [
        ["1783957583024089247", "{\"@timestamp\":\"2026-07-13T15:46:23Z\",\"message\":\"Fila de pedidos crescendo: 465 itens, considere escalar\",\"level\":\"WARN\",\"service\":\"micro-pedidos\"}"],
        ["1783957543024004593", "{\"@timestamp\":\"2026-07-13T15:45:43Z\",\"message\":\"Fila de pedidos crescendo: 415 itens, considere escalar\",\"level\":\"WARN\",\"service\":\"micro-pedidos\"}"],
        ["1783957463024160561", "{\"@timestamp\":\"2026-07-13T15:44:23Z\",\"message\":\"Fila de pedidos crescendo: 495 itens, considere escalar\",\"level\":\"WARN\",\"service\":\"micro-pedidos\"}"]
      ]
    }]
  }
}
```

O parâmetro `query` (a expressão LogQL) é definido pelo modelo a cada execução
(`$fromAI`) — visível na aba **Input** do mesmo nó. Exemplo típico gerado pela
IA neste cenário: `{cluster="k3s-lab", level=~"warn|error"}`.
