package gt.edu.umg.desarrolloweb.sync.service;

import gt.edu.umg.desarrolloweb.sync.api.dto.EstadoJobDto;
import gt.edu.umg.desarrolloweb.sync.api.dto.SyncJobDto;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Capa de negocio de los jobs.
 *
 * VERSION INICIAL (setup): solo registra el job en memoria con estado PENDIENTE.
 * En los siguientes pasos aqui se agregara:
 *   - la ejecucion en segundo plano (@Async),
 *   - la lectura del monolito, el diff y los upserts en DynamoDB,
 *   - la actualizacion del progreso (0-100).
 */
@Service
public class SyncJobService {

    // ConcurrentHashMap: seguro cuando varios hilos leen/escriben a la vez
    private final Map<UUID, SyncJobDto> jobs = new ConcurrentHashMap<>();

    public SyncJobDto crearJob() {
        SyncJobDto job = new SyncJobDto()
                .jobId(UUID.randomUUID())
                .estado(EstadoJobDto.PENDIENTE)
                .progreso(0)
                .totalRegistros(0)
                .procesados(0)
                .insertados(0)
                .actualizados(0)
                .sinCambios(0)
                .creadoEn(OffsetDateTime.now());
        jobs.put(job.getJobId(), job);
        return job;
    }

    public SyncJobDto obtenerJob(UUID jobId) {
        SyncJobDto job = jobs.get(jobId);
        if (job == null) {
            throw new JobNotFoundException(jobId);
        }
        return job;
    }
}
