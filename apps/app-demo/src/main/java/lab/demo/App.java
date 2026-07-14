package lab.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Random;

@SpringBootApplication
@EnableScheduling
@RestController
public class App {
    private static final Logger log = LoggerFactory.getLogger(App.class);
    private final Random rnd = new Random();

    public static void main(String[] args) { SpringApplication.run(App.class, args); }

    @GetMapping("/health")
    public Map<String, String> health() {
        log.info("Health check executado com sucesso");
        return Map.of("status", "UP", "app", "app-demo");
    }

    @GetMapping("/api/pedido/{id}")
    public Map<String, Object> pedido(@PathVariable int id) {
        log.info("Consultando pedido id={}", id);
        if (id > 1000) {
            log.warn("Pedido id={} fora da faixa esperada (max=1000)", id);
        }
        return Map.of("pedido", id, "status", "PROCESSADO");
    }

    @GetMapping("/api/erro")
    public String erro() {
        log.error("Falha ao processar requisicao critica", 
            new IllegalStateException("Conexao com gateway de pagamento recusada: timeout apos 30s"));
        throw new IllegalStateException("Erro simulado para o lab AIOps");
    }

    @GetMapping("/api/npe")
    public String npe() {
        String s = null;
        return s.toUpperCase(); // NullPointerException real, stack trace completo
    }

    // Gera trafego de log continuo: INFO frequente, WARN ocasional
    @Scheduled(fixedRate = 15000)
    public void batimento() {
        int n = rnd.nextInt(100);
        if (n < 85) log.info("Processamento de rotina concluido: {} registros em {}ms", n * 10, 50 + n);
        else log.warn("Fila de processamento acima do normal: {} itens pendentes", n * 7);
    }
}
