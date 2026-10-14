package gt.edu.umg.desarrolloweb.sync.service;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Ejecuta la replicacion en SEGUNDO PLANO.
 *
 * @Async("syncExecutor"): cuando el controller llama a ejecutar(...), Spring NO espera;
 * le pasa el trabajo a un hilo del pool "syncExecutor" y regresa de inmediato.
 * Por eso el POST puede responder 202 al instante.
 *
 * Va en una clase aparte (no dentro de SyncJobService) porque @Async solo funciona
 * cuando el metodo se llama desde OTRO bean.
 */
@Component
public class SyncJobRunner {

    private static final Logger log = LoggerFactory.getLogger(SyncJobRunner.class);

    // ---- SIMULACION (se reemplaza manana por la lectura real del monolito) ----
    private static final int TOTAL_SIMULADO = 20;
    private static final long PAUSA_MS = 500;   // 20 x 0.5 s = ~10 segundos

    private final SyncJobService jobService;

    public SyncJobRunner(SyncJobService jobService) {
        this.jobService = jobService;
    }

    @Async("syncExecutor")
    public void ejecutar(UUID jobId) {
        log.info("Job {} iniciado", jobId);
        try {
            jobService.marcarEnProgreso(jobId, TOTAL_SIMULADO);

            for (int i = 1; i <= TOTAL_SIMULADO; i++) {
                Thread.sleep(PAUSA_MS);   // SIMULACION: aqui ira leer resumen + diff + upsert
                jobService.registrarAvance(jobId, i, i, 0, 0);
                log.debug("Job {} avance {}/{}", jobId, i, TOTAL_SIMULADO);
            }

            jobService.completar(jobId);
            log.info("Job {} completado", jobId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            jobService.fallar(jobId, "El job fue interrumpido");
        } catch (Exception e) {
            // Cualquier error deja el job en FALLIDO (con mensaje) en vez de quedarse "colgado"
            log.error("Job {} fallo", jobId, e);
            jobService.fallar(jobId, e.getMessage());
        }
    }
}
