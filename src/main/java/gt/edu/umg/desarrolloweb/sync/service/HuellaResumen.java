package gt.edu.umg.desarrolloweb.sync.service;

import gt.edu.umg.desarrolloweb.sync.model.ResumenContador;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Calcula la "huella digital" (hash SHA-256) de un resumen.
 *
 * Mismos datos -> misma huella, siempre. Si cambia UN solo dato (un pago, una lectura,
 * el nombre del cliente...), la huella cambia por completo. Comparar huellas es la forma
 * de saber QUE cambio sin depender de fechas (lecturas y pagos no tienen actualizado_en).
 *
 * Solo entran datos de negocio; nunca fechas de sincronizacion ni el jobId
 * (si no, la huella cambiaria en cada corrida y todo pareceria "cambiado").
 */
public final class HuellaResumen {

    private HuellaResumen() {
    }

    public static String calcular(ResumenContador r) {
        String contenido = String.join("|",
                String.valueOf(r.contadorId()),
                texto(r.numeroRegistro()),
                texto(r.direccionServicio()),
                texto(r.sector()),
                String.valueOf(r.contadorActivo()),
                String.valueOf(r.clienteId()),
                texto(r.clienteNombre()),
                String.valueOf(r.totalLecturas()),
                String.valueOf(r.lecturasPendientes()),
                dinero(r.totalFacturado()),
                dinero(r.totalPagado()),
                dinero(r.saldo()),
                texto(r.ultimoPeriodo()),
                dinero(r.ultimaLectura()));
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] bytes = sha256.digest(contenido.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor;
    }

    // 45, 45.0 y 45.00 deben dar la misma huella -> siempre 2 decimales
    private static String dinero(BigDecimal valor) {
        return valor == null ? "" : valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
