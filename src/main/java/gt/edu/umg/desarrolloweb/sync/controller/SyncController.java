package gt.edu.umg.desarrolloweb.sync.controller;

import gt.edu.umg.desarrolloweb.sync.api.dto.SyncJobAceptadoDto;
import gt.edu.umg.desarrolloweb.sync.api.dto.SyncJobDto;
import gt.edu.umg.desarrolloweb.sync.api.spec.SyncApi;
import gt.edu.umg.desarrolloweb.sync.service.SyncJobService;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Capa REST. Implementa la interfaz GENERADA desde sync-api.yaml.
 * No hay @PostMapping ni @GetMapping aqui: las rutas y validaciones vienen del contrato.
 * Si el YAML cambia, esta clase deja de compilar hasta implementarlo.
 */
@RestController
public class SyncController implements SyncApi {

    private final SyncJobService syncJobService;

    public SyncController(SyncJobService syncJobService) {
        this.syncJobService = syncJobService;
    }

    @Override
    public ResponseEntity<SyncJobAceptadoDto> iniciarReplicacionResumen() {
        SyncJobDto job = syncJobService.crearJob();
        String url = "/api/sync/jobs/" + job.getJobId();

        SyncJobAceptadoDto body = new SyncJobAceptadoDto()
                .jobId(job.getJobId())
                .estado(job.getEstado())
                .consultarEn(url);

        // 202 Accepted + header Location: "recibido, se procesa en segundo plano"
        return ResponseEntity.accepted().location(URI.create(url)).body(body);
    }

    @Override
    public ResponseEntity<SyncJobDto> obtenerEstadoJob(UUID jobId) {
        return ResponseEntity.ok(syncJobService.obtenerJob(jobId));
    }
}
