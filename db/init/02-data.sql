-- Datos de prueba: 5 clientes, 8 contadores, lecturas de 2026-08 y 2026-09, algunos pagos.
-- Tarifa simple Q2.50 por m3 (monto = consumo * 2.50).
SET NAMES utf8mb4;

INSERT INTO roles (id, nombre, descripcion) VALUES
  (1, 'Administrador', 'Acceso total'),
  (2, 'Secretaria', 'Registra pagos'),
  (3, 'Lector', 'Registra lecturas');

INSERT INTO usuarios (id, rol_id, nombre, email, password_hash) VALUES
  (1, 1, 'Admin',           'admin@oficina-agua.local',      'hash-no-usado'),
  (2, 3, 'Pedro Lector',    'lector@oficina-agua.local',     'hash-no-usado'),
  (3, 2, 'Rosa Secretaria', 'secretaria@oficina-agua.local', 'hash-no-usado');

INSERT INTO clientes (id, nombre, telefono, direccion_principal) VALUES
  (1, 'Maria Lopez',    '5555-0001', '4a calle 5-20 zona 1'),
  (2, 'Jose Perez',     '5555-0002', '10a avenida 3-45 zona 10'),
  (3, 'Ana Garcia',     NULL,        'Km 15 carretera a El Salvador'),
  (4, 'Carlos Ramirez', '5555-0004', '2a calle 1-10 zona 3'),
  (5, 'Lucia Morales',  '5555-0005', 'Colonia Primavera casa 21');

INSERT INTO contadores (id, cliente_id, numero_registro, direccion_servicio, sector) VALUES
  (1, 1, 'CNT-0001', '4a calle 5-20 zona 1',          'Centro'),
  (2, 1, 'CNT-0002', 'Lote 3 Aldea El Rosario',       'El Rosario'),
  (3, 2, 'CNT-0003', '10a avenida 3-45 zona 10',      'Centro'),
  (4, 3, 'CNT-0004', 'Km 15 carretera a El Salvador', 'Las Flores'),
  (5, 3, 'CNT-0005', 'Callejon 2 casa 8',             'Las Flores'),
  (6, 4, 'CNT-0006', '2a calle 1-10 zona 3',          'San Jose'),
  (7, 5, 'CNT-0007', 'Colonia Primavera casa 21',     'Primavera'),
  (8, 5, 'CNT-0008', 'Colonia Primavera casa 22',     'Primavera');   -- sin lecturas todavia

INSERT INTO tarifas (id, monto_por_unidad, vigente_desde, vigente_hasta) VALUES
  (1, 2.50, '2026-01-01', NULL);

INSERT INTO lecturas (id, contador_id, usuario_lector_id, periodo, fecha_lectura, lectura_anterior, lectura_actual, consumo, tarifa_id, monto) VALUES
  ( 1, 1, 2, '2026-08', '2026-08-28', 100.00, 118.00, 18.00, 1, 45.00),
  ( 2, 1, 2, '2026-09', '2026-09-28', 118.00, 133.00, 15.00, 1, 37.50),
  ( 3, 2, 2, '2026-08', '2026-08-28',  50.00,  60.00, 10.00, 1, 25.00),
  ( 4, 2, 2, '2026-09', '2026-09-28',  60.00,  72.00, 12.00, 1, 30.00),
  ( 5, 3, 2, '2026-08', '2026-08-28', 200.00, 225.00, 25.00, 1, 62.50),
  ( 6, 3, 2, '2026-09', '2026-09-28', 225.00, 247.00, 22.00, 1, 55.00),
  ( 7, 4, 2, '2026-08', '2026-08-28',  80.00,  92.00, 12.00, 1, 30.00),
  ( 8, 4, 2, '2026-09', '2026-09-28',  92.00, 101.00,  9.00, 1, 22.50),
  ( 9, 5, 2, '2026-08', '2026-08-28',  10.00,  14.00,  4.00, 1, 10.00),
  (10, 5, 2, '2026-09', '2026-09-28',  14.00,  19.00,  5.00, 1, 12.50),
  (11, 6, 2, '2026-08', '2026-08-28', 300.00, 330.00, 30.00, 1, 75.00),
  (12, 6, 2, '2026-09', '2026-09-28', 330.00, 356.00, 26.00, 1, 65.00),
  (13, 7, 2, '2026-08', '2026-08-28',   0.00,   8.00,  8.00, 1, 20.00),
  (14, 7, 2, '2026-09', '2026-09-28',   8.00,  17.00,  9.00, 1, 22.50);

-- Pagos: casi todos pagaron agosto; Jose Perez (CNT-0003) tambien septiembre; Ana (CNT-0004) no ha pagado nada.
INSERT INTO pagos (lectura_id, usuario_registro_id, monto, fecha_pago, metodo, numero_boleta) VALUES
  ( 1, 3, 45.00, '2026-09-05', 'efectivo',      NULL),
  ( 3, 3, 25.00, '2026-09-06', 'deposito',      'BOL-1001'),
  ( 5, 3, 62.50, '2026-09-06', 'efectivo',      NULL),
  ( 6, 3, 55.00, '2026-10-04', 'transferencia', 'TRF-2001'),
  ( 9, 3, 10.00, '2026-09-08', 'efectivo',      NULL),
  (11, 3, 75.00, '2026-09-10', 'efectivo',      NULL),
  (13, 3, 20.00, '2026-09-12', 'deposito',      'BOL-1002');
