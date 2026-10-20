package gt.edu.umg.desarrolloweb.sync.service;

import gt.edu.umg.desarrolloweb.sync.api.dto.EstadoJobDto;
import gt.edu.umg.desarrolloweb.sync.api.dto.ResultadoRegistroDto;
import gt.edu.umg.desarrolloweb.sync.api.dto.SyncJobDto;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;

/**
 * Capa de negocio de los jobs: los crea, los guarda en memoria y actualiza su estado.
 * El trabajo pesado lo hace SyncJobRunner en otro hilo; este servicio solo lleva el registro.
 */
@Service
public class SyncJobService {

    // ConcurrentHashMap: seguro cuando el hilo del job escribe y el hilo del GET lee a la vez
    private final Map<UUID, SyncJobDto> jobs = new ConcurrentHashMap<>();

    /**
     * synchronized: si llegan dos POST al mismo tiempo, entran de uno en uno.
     * Asi no se pueden colar dos jobs activos.
     */
    public synchronized SyncJobDto crearJob(String urlBase) {
        boolean hayJobActivo = jobs.values().stream()
                .anyMatch(j -> j.getEstado() == EstadoJobDto.PENDIENTE
                            || j.getEstado() == EstadoJobDto.EN_PROGRESO);
        if (hayJobActivo) {
            throw new JobEnCursoException();
        }

        UUID jobId = UUID.randomUUID();
        SyncJobDto job = new SyncJobDto()
                .jobId(jobId)
                .estado(EstadoJobDto.PENDIENTE)
                .progreso(0)
                .totalRegistros(0)
                .procesados(0)
                .insertados(0)
                .actualizados(0)
                .sinCambios(0)
                .creadoEn(OffsetDateTime.now())
                .verEn(urlBase + "/progreso.html?jobId=" + jobId)
                // CopyOnWriteArrayList: el hilo del job agrega resultados mientras el GET
                // los convierte a JSON; con un ArrayList normal eso podria lanzar un error.
                .resultados(new CopyOnWriteArrayList<>());
        jobs.put(jobId, job);
        return job;
    }

    public SyncJobDto obtenerJob(UUID jobId) {
        SyncJobDto job = jobs.get(jobId);
        if (job == null) {
            throw new JobNotFoundException(jobId);
        }
        return job;
    }

    // ---- Metodos que usa el SyncJobRunner (desde el hilo del job) ----

    public void marcarEnProgreso(UUID jobId, int totalRegistros) {
        actualizar(jobId, job -> job
                .estado(EstadoJobDto.EN_PROGRESO)
                .iniciadoEn(OffsetDateTime.now())
                .totalRegistros(totalRegistros));
    }

    public void registrarAvance(UUID jobId, ResultadoRegistroDto resultado, int procesados,
                                int insertados, int actualizados, int sinCambios) {
        actualizar(jobId, job -> {
            job.getResultados().add(resultado);   // la pagina web muestra esta fila de inmediato
            job.procesados(procesados)
               .insertados(insertados)
               .actualizados(actualizados)
               .sinCambios(sinCambios);
            int total = job.getTotalRegistros();
            // Porcentaje entero 0-100. Si no hay registros, se considera 100%.
            job.progreso(total == 0 ? 100 : procesados * 100 / total);
        });
    }

    public void completar(UUID jobId) {
        actualizar(jobId, job -> job
                .estado(EstadoJobDto.COMPLETADO)
                .progreso(100)
                .finalizadoEn(OffsetDateTime.now()));
    }

    public void fallar(UUID jobId, String mensaje) {
        actualizar(jobId, job -> job
                .estado(EstadoJobDto.FALLIDO)
                .mensaje(mensaje)
                .finalizadoEn(OffsetDateTime.now()));
    }

    /** computeIfPresent: aplica el cambio de forma atomica sobre ese job. */
    private void actualizar(UUID jobId, Consumer<SyncJobDto> cambio) {
        jobs.computeIfPresent(jobId, (id, job) -> {
            cambio.accept(job);
            return job;
        });
    }
}
