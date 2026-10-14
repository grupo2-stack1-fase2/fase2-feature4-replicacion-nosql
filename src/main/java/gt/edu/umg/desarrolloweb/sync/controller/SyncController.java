package gt.edu.umg.desarrolloweb.sync.controller;

import gt.edu.umg.desarrolloweb.sync.api.dto.SyncJobAceptadoDto;
import gt.edu.umg.desarrolloweb.sync.api.dto.SyncJobDto;
import gt.edu.umg.desarrolloweb.sync.api.spec.SyncApi;
import gt.edu.umg.desarrolloweb.sync.service.SyncJobRunner;
import gt.edu.umg.desarrolloweb.sync.service.SyncJobService;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Capa REST. Implementa la interfaz GENERADA desde sync-api.yaml.
 * No hay @PostMapping ni @GetMapping aqui: las rutas y validaciones vienen del contrato.
 */
@RestController
public class SyncController implements SyncApi {

    private final SyncJobService syncJobService;
    private final SyncJobRunner syncJobRunner;

    public SyncController(SyncJobService syncJobService, SyncJobRunner syncJobRunner) {
        this.syncJobService = syncJobService;
        this.syncJobRunner = syncJobRunner;
    }

    @Override
    public ResponseEntity<SyncJobAceptadoDto> iniciarReplicacionResumen() {
        // 1. Registrar el job (PENDIENTE). Si ya hay uno activo, lanza 409.
        SyncJobDto job = syncJobService.crearJob();
        String url = "/api/sync/jobs/" + job.getJobId();

        // 2. Armar la respuesta ANTES de lanzar el hilo (asi el estado sale PENDIENTE)
        SyncJobAceptadoDto body = new SyncJobAceptadoDto()
                .jobId(job.getJobId())
                .estado(job.getEstado())
                .consultarEn(url);

        // 3. Lanzar el trabajo en segundo plano: esta llamada regresa de inmediato
        syncJobRunner.ejecutar(job.getJobId());

        // 4. 202 Accepted + Location
        return ResponseEntity.accepted().location(URI.create(url)).body(body);
    }

    @Override
    public ResponseEntity<SyncJobDto> obtenerEstadoJob(UUID jobId) {
        return ResponseEntity.ok(syncJobService.obtenerJob(jobId));
    }
}
