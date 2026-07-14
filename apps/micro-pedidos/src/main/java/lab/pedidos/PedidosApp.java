package lab.pedidos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@SpringBootApplication
@EnableScheduling
@RestController
public class PedidosApp {
    private static final Logger log = LoggerFactory.getLogger(PedidosApp.class);
    private final Random rnd = new Random();

    public static void main(String[] args) { SpringApplication.run(PedidosApp.class, args); }

    @GetMapping("/health")
    public Map<String, String> health() { return Map.of("status", "UP", "service", "micro-pedidos"); }

    @PostMapping("/api/pedidos")
    public Map<String, Object> criar(@RequestBody(required = false) Map<String, Object> body) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        int sorte = rnd.nextInt(10);
        if (sorte < 7) {
            log.info("Pedido {} criado com sucesso, itens={}", id, 1 + rnd.nextInt(5));
            return Map.of("pedidoId", id, "status", "CRIADO");
        } else if (sorte < 9) {
            log.warn("Pedido {} criado com atraso: servico de estoque lento ({}ms)", id, 800 + rnd.nextInt(2000));
            return Map.of("pedidoId", id, "status", "CRIADO_COM_ATRASO");
        }
        log.error("Pedido {} FALHOU: servico de estoque indisponivel apos 3 tentativas", id,
            new RuntimeException("estoque-service: connection refused (10.43.0.99:8081)"));
        throw new IllegalStateException("Falha ao reservar estoque para pedido " + id);
    }

    @GetMapping("/api/crash")
    public String crash() {
        log.error("Recebido comando de crash - encerrando pod para teste de resiliencia");
        new Thread(() -> { try { Thread.sleep(500); } catch (Exception e) {} System.exit(1); }).start();
        return "Pod vai morrer em 500ms... o Kubernetes vai reagir 😉";
    }

    @Scheduled(fixedRate = 20000)
    public void processadorFila() {
        int n = rnd.nextInt(100);
        if (n < 80) log.info("Fila de pedidos processada: {} pendentes", n);
        else log.warn("Fila de pedidos crescendo: {} itens, considere escalar", n * 5);
    }
}
