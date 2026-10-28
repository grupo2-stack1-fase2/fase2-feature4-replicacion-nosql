package gt.edu.umg.desarrolloweb.sync.service;

import gt.edu.umg.desarrolloweb.sync.api.dto.AccionRegistroDto;
import gt.edu.umg.desarrolloweb.sync.api.dto.ResultadoRegistroDto;
import gt.edu.umg.desarrolloweb.sync.model.ResumenContador;
import gt.edu.umg.desarrolloweb.sync.repository.ResumenDestinoRepository;
import gt.edu.umg.desarrolloweb.sync.repository.ResumenOrigenRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Ejecuta la replicacion en SEGUNDO PLANO (@Async, hilo "sync-job-1").
 *
 * Algoritmo (diff + upsert, nunca borra):
 *   1. Leer el resumen de cada contador desde el monolito (solo lectura).
 *   2. Para cada resumen, calcular su huella (SHA-256).
 *   3. Comparar con la huella guardada en DynamoDB:
 *        - no existe       -> putItem  -> insertados++
 *        - huella distinta -> putItem  -> actualizados++
 *        - huella igual    -> no se escribe nada -> sinCambios++
 *   4. Actualizar el progreso y guardar el resultado de cada registro
 *      (la pagina progreso.html los muestra en vivo).
 */
@Component
public class SyncJobRunner {

    private static final Logger log = LoggerFactory.getLogger(SyncJobRunner.class);

    private final SyncJobService jobService;
    private final ResumenOrigenRepository origen;
    private final ResumenDestinoRepository destino;
    private final long pausaPorRegistroMs;

    public SyncJobRunner(SyncJobService jobService,
                         ResumenOrigenRepository origen,
                         ResumenDestinoRepository destino,
                         @Value("${sync.pausa-por-registro-ms:0}") long pausaPorRegistroMs) {
        this.jobService = jobService;
        this.origen = origen;
        this.destino = destino;
        this.pausaPorRegistroMs = pausaPorRegistroMs;
    }

    @Async("syncExecutor")
    public void ejecutar(UUID jobId) {
        log.info("Job {} iniciado", jobId);
        try {
            destino.asegurarTabla();

            List<ResumenContador> resumenes = origen.leerResumenes();
            jobService.marcarEnProgreso(jobId, resumenes.size());
            log.info("Job {}: {} resumenes leidos del monolito", jobId, resumenes.size());

            int procesados = 0;
            int insertados = 0;
            int actualizados = 0;
            int sinCambios = 0;

            for (ResumenContador resumen : resumenes) {
                String huellaNueva = HuellaResumen.calcular(resumen);
                Optional<String> huellaGuardada = destino.obtenerHuella(resumen.contadorId());

                AccionRegistroDto accion;
                if (huellaGuardada.isEmpty()) {
                    destino.guardar(resumen, huellaNueva, jobId);
                    insertados++;
                    accion = AccionRegistroDto.INSERTADO;
                    log.info("Job {}: contador {} INSERTADO", jobId, resumen.numeroRegistro());
                } else if (!huellaGuardada.get().equals(huellaNueva)) {
                    destino.guardar(resumen, huellaNueva, jobId);
                    actualizados++;
                    accion = AccionRegistroDto.ACTUALIZADO;
                    log.info("Job {}: contador {} ACTUALIZADO", jobId, resumen.numeroRegistro());
                } else {
                    sinCambios++;
                    accion = AccionRegistroDto.SIN_CAMBIOS;
                }

                procesados++;
                jobService.registrarAvance(jobId, aResultado(procesados, resumen, accion),
                        procesados, insertados, actualizados, sinCambios);

                if (pausaPorRegistroMs > 0) {
                    Thread.sleep(pausaPorRegistroMs);   // solo para que la demo se pueda observar
                }
            }

            jobService.completar(jobId);
            log.info("Job {} completado: {} insertados, {} actualizados, {} sin cambios",
                    jobId, insertados, actualizados, sinCambios);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            jobService.fallar(jobId, "El job fue interrumpido");
        } catch (Exception e) {
            // BD caida, DynamoDB apagado, etc.: el job queda FALLIDO con mensaje (no "colgado")
            log.error("Job {} fallo", jobId, e);
            jobService.fallar(jobId, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /** Fila que vera la pagina de progreso: datos del contador + que hizo el diff. */
    private static ResultadoRegistroDto aResultado(int orden, ResumenContador r,
                                                   AccionRegistroDto accion) {
        return new ResultadoRegistroDto()
                .orden(orden)
                .contadorId(r.contadorId())
                .numeroRegistro(r.numeroRegistro())
                .clienteNombre(r.clienteNombre())
                .sector(r.sector())
                .totalFacturado(r.totalFacturado())
                .totalPagado(r.totalPagado())
                .saldo(r.saldo())
                .accion(accion);
    }
}
