package gt.edu.umg.desarrolloweb.sync.controller;

import gt.edu.umg.desarrolloweb.sync.api.dto.ErrorDto;
import gt.edu.umg.desarrolloweb.sync.service.JobEnCursoException;
import gt.edu.umg.desarrolloweb.sync.service.JobNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Traduce excepciones al schema Error del contrato (status, message, details). */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(JobNotFoundException.class)
    public ResponseEntity<ErrorDto> notFound(JobNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // POST mientras otro job sigue PENDIENTE o EN_PROGRESO
    @ExceptionHandler(JobEnCursoException.class)
    public ResponseEntity<ErrorDto> conflict(JobEnCursoException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    // jobId que no es un UUID valido, por ejemplo /api/sync/jobs/abc
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDto> badRequest(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST, "Parametro invalido: " + ex.getName());
    }

    // Cualquier otro error: 500 y el detalle solo al log (nunca al cliente)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> internal(Exception ex) {
        log.error("Error inesperado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error inesperado");
    }

    private ResponseEntity<ErrorDto> build(HttpStatus status, String message) {
        ErrorDto error = new ErrorDto().status(status.value()).message(message);
        return ResponseEntity.status(status).body(error);
    }
}
