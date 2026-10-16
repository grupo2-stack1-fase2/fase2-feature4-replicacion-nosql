-- Copia LOCAL del esquema de AQUORA (fase 1), tomado de app/Database/Migrations.
-- Solo para desarrollo: el monolito real NO se toca (regla de oro).
SET NAMES utf8mb4;

CREATE TABLE roles (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(50) NOT NULL,
  descripcion VARCHAR(150) NULL,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE usuarios (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  rol_id INT UNSIGNED NOT NULL,
  nombre VARCHAR(120) NOT NULL,
  email VARCHAR(150) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_usuarios_email (email),
  CONSTRAINT fk_usuarios_rol FOREIGN KEY (rol_id) REFERENCES roles (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE clientes (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(150) NOT NULL,
  telefono VARCHAR(20) NULL,
  direccion_principal VARCHAR(255) NOT NULL,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  token VARCHAR(64) NULL,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_clientes_token (token),
  KEY idx_clientes_nombre (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE contadores (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  cliente_id INT UNSIGNED NOT NULL,
  numero_registro VARCHAR(30) NOT NULL,
  direccion_servicio VARCHAR(255) NOT NULL,
  sector VARCHAR(100) NULL,
  activo TINYINT(1) NOT NULL DEFAULT 1,
  token VARCHAR(64) NULL,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_contadores_numero (numero_registro),
  UNIQUE KEY uq_contadores_token (token),
  KEY idx_contadores_sector (sector),
  CONSTRAINT fk_contadores_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE tarifas (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  monto_por_unidad DECIMAL(10,2) NOT NULL COMMENT 'Precio por m3 de consumo',
  vigente_desde DATE NOT NULL,
  vigente_hasta DATE NULL COMMENT 'NULL = todavia vigente',
  activo TINYINT(1) NOT NULL DEFAULT 1,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_tarifas_vigencia (vigente_desde, vigente_hasta)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE lecturas (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  contador_id INT UNSIGNED NOT NULL,
  usuario_lector_id INT UNSIGNED NOT NULL COMMENT 'Quien tomo la lectura',
  periodo CHAR(7) NOT NULL COMMENT 'Formato YYYY-MM',
  fecha_lectura DATE NOT NULL,
  lectura_anterior DECIMAL(10,2) UNSIGNED NOT NULL DEFAULT 0,
  lectura_actual DECIMAL(10,2) UNSIGNED NOT NULL,
  consumo DECIMAL(10,2) UNSIGNED NOT NULL,
  tarifa_id INT UNSIGNED NOT NULL,
  monto DECIMAL(10,2) NOT NULL,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_lecturas_contador_periodo (contador_id, periodo),
  KEY idx_lecturas_periodo (periodo),
  CONSTRAINT fk_lecturas_contador FOREIGN KEY (contador_id) REFERENCES contadores (id) ON DELETE RESTRICT,
  CONSTRAINT fk_lecturas_usuario_lector FOREIGN KEY (usuario_lector_id) REFERENCES usuarios (id) ON DELETE RESTRICT,
  CONSTRAINT fk_lecturas_tarifa FOREIGN KEY (tarifa_id) REFERENCES tarifas (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE pagos (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  lectura_id INT UNSIGNED NOT NULL,
  usuario_registro_id INT UNSIGNED NOT NULL COMMENT 'Secretaria que registro el pago',
  monto DECIMAL(10,2) NOT NULL,
  fecha_pago DATE NOT NULL,
  metodo ENUM('efectivo','deposito','transferencia') NOT NULL DEFAULT 'efectivo',
  numero_boleta VARCHAR(50) NULL,
  observaciones VARCHAR(255) NULL,
  token VARCHAR(32) NULL COMMENT 'Token de idempotencia',
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_pagos_lectura (lectura_id),
  CONSTRAINT fk_pagos_lectura FOREIGN KEY (lectura_id) REFERENCES lecturas (id) ON DELETE RESTRICT,
  CONSTRAINT fk_pagos_usuario_registro FOREIGN KEY (usuario_registro_id) REFERENCES usuarios (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
