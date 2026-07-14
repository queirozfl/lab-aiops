// Media type "n8n AIOps" — Type: Webhook
// Parameters esperados (Name → Value):
//   url        → http://192.168.15.203:5678/webhook/zabbix-alerta   (minúsculo!)
//   host       → {HOST.NAME}
//   trigger    → {TRIGGER.NAME}
//   severity   → {TRIGGER.SEVERITY}
//   status     → {TRIGGER.STATUS}
//   item_value → {ITEM.LASTVALUE1}
//   event_id   → {EVENT.ID}
//   event_time → {EVENT.DATE} {EVENT.TIME}
// Obs.: Message templates (Problem e Problem recovery) devem existir,
// mesmo que o corpo não seja usado pelo script.
var params = JSON.parse(value);
var req = new HttpRequest();
req.addHeader('Content-Type: application/json');
var resp = req.post(params.url, JSON.stringify(params));
if (req.getStatus() !== 200) {
    throw 'n8n retornou status ' + req.getStatus() + ': ' + resp;
}
return 'OK';
