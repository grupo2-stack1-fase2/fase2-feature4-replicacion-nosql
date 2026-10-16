-- DEMO del diff: simula actividad normal en AQUORA entre dos replicaciones.
-- Se ejecuta como root (como si fuera la aplicacion AQUORA), NO como el sync-service.
--   1) Maria Lopez paga septiembre del CNT-0001  -> su resumen CAMBIA  (actualizado)
--   2) Se registra un contador nuevo CNT-0009    -> resumen NUEVO      (insertado)
--   Los demas contadores no cambian                                    (sinCambios)
INSERT INTO pagos (lectura_id, usuario_registro_id, monto, fecha_pago, metodo, numero_boleta)
VALUES (2, 3, 37.50, CURDATE(), 'efectivo', NULL);

INSERT INTO contadores (cliente_id, numero_registro, direccion_servicio, sector)
VALUES (2, 'CNT-0009', 'Colonia Las Brisas casa 5', 'Las Brisas');
