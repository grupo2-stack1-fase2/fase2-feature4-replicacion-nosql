package gt.edu.umg.desarrolloweb.sync.service;

/** Se lanza si se pide una replicacion mientras otra sigue activa -> 409. */
public class JobEnCursoException extends RuntimeException {

    public JobEnCursoException() {
        super("Ya hay una replicacion en curso; espere a que termine");
    }
}
