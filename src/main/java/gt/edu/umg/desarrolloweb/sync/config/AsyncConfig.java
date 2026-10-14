package gt.edu.umg.desarrolloweb.sync.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Activa @Async y define el "equipo de trabajadores" (pool de hilos) que ejecuta los jobs.
 *
 * Solo 1 hilo: las replicaciones corren de una en una (ademas el servicio rechaza
 * con 409 una segunda mientras hay otra activa).
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "syncExecutor")
    public Executor syncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(5);
        executor.setThreadNamePrefix("sync-job-");   // asi se ve en los logs
        executor.initialize();
        return executor;
    }
}
