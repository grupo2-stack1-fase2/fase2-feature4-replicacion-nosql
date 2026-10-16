package gt.edu.umg.desarrolloweb.sync.model;

import java.math.BigDecimal;

/**
 * Resumen de cuenta de UN contador: lo que se replica a DynamoDB.
 *
 * "record" (Java 16+): clase de solo datos. Java genera solo el constructor,
 * los getters (contadorId(), clienteNombre()...), equals, hashCode y toString.
 */
public record ResumenContador(
        Long contadorId,
        String numeroRegistro,
        String direccionServicio,
        String sector,
        boolean contadorActivo,
        Long clienteId,
        String clienteNombre,
        int totalLecturas,
        int lecturasPendientes,
        BigDecimal totalFacturado,
        BigDecimal totalPagado,
        String ultimoPeriodo,
        BigDecimal ultimaLectura) {

    /** Lo que el cliente debe en este contador. */
    public BigDecimal saldo() {
        return totalFacturado.subtract(totalPagado);
    }
}
