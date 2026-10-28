package gt.edu.umg.desarrolloweb.sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de arranque del sync-service (Feature 4).
 * Va en el paquete raiz: Spring escanea desde aqui hacia abajo (controller, service...).
 */
@SpringBootApplication
public class SyncServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SyncServiceApplication.class, args);
    }
}
