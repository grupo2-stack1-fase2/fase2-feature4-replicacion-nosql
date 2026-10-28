package gt.edu.umg.desarrolloweb.sync.service;

import java.util.UUID;

/** Se lanza cuando se consulta un jobId que no existe -> 404. */
public class JobNotFoundException extends RuntimeException {

    public JobNotFoundException(UUID jobId) {
        super("No existe un job con id " + jobId);
    }
}
