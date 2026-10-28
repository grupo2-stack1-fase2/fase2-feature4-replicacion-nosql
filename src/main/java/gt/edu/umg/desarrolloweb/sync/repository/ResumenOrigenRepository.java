package gt.edu.umg.desarrolloweb.sync.repository;

import gt.edu.umg.desarrolloweb.sync.model.ResumenContador;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * ORIGEN: lee el monolito (AQUORA). Solo hace SELECT.
 *
 * Usamos JdbcTemplate (SQL directo) en vez de entidades JPA porque necesitamos UNA consulta
 * que agrupa 4 tablas; no vamos a crear, editar ni borrar nada. Ademas el usuario sync_reader
 * solo tiene permiso SELECT: aunque alguien intentara escribir, la BD lo rechazaria.
 * Todo corre como prepared statement (igual que PDO / JPA): sin inyeccion SQL.
 */
@Repository
public class ResumenOrigenRepository {

    // pagos.lectura_id es UNIQUE (1 pago por lectura) -> el LEFT JOIN no duplica montos.
    private static final String SQL_RESUMEN = """
            SELECT c.id                       AS contador_id,
                   c.numero_registro,
                   c.direccion_servicio,
                   c.sector,
                   c.activo                   AS contador_activo,
                   cl.id                      AS cliente_id,
                   cl.nombre                  AS cliente_nombre,
                   COUNT(l.id)                AS total_lecturas,
                   COUNT(l.id) - COUNT(p.id)  AS lecturas_pendientes,
                   COALESCE(SUM(l.monto), 0)  AS total_facturado,
                   COALESCE(SUM(p.monto), 0)  AS total_pagado,
                   MAX(l.periodo)             AS ultimo_periodo,
                   (SELECT l2.lectura_actual
                      FROM lecturas l2
                     WHERE l2.contador_id = c.id
                     ORDER BY l2.periodo DESC
                     LIMIT 1)                 AS ultima_lectura
              FROM contadores c
              JOIN clientes cl ON cl.id = c.cliente_id
              LEFT JOIN lecturas l ON l.contador_id = c.id
              LEFT JOIN pagos p ON p.lectura_id = l.id
             GROUP BY c.id, c.numero_registro, c.direccion_servicio, c.sector, c.activo,
                      cl.id, cl.nombre
             ORDER BY c.id
            """;

    private final JdbcTemplate jdbc;

    public ResumenOrigenRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<ResumenContador> leerResumenes() {
        return jdbc.query(SQL_RESUMEN, (rs, numFila) -> new ResumenContador(
                rs.getLong("contador_id"),
                rs.getString("numero_registro"),
                rs.getString("direccion_servicio"),
                rs.getString("sector"),
                rs.getBoolean("contador_activo"),
                rs.getLong("cliente_id"),
                rs.getString("cliente_nombre"),
                rs.getInt("total_lecturas"),
                rs.getInt("lecturas_pendientes"),
                rs.getBigDecimal("total_facturado"),
                rs.getBigDecimal("total_pagado"),
                rs.getString("ultimo_periodo"),
                rs.getBigDecimal("ultima_lectura")));
    }
}
