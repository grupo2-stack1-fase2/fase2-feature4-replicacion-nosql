-- Usuario del sync-service: SOLO puede leer (SELECT). Aunque el codigo tuviera un error,
-- la base de datos le impediria modificar el monolito. Minimo privilegio.
CREATE USER IF NOT EXISTS 'sync_reader'@'%' IDENTIFIED BY 'sync_reader_pw';
GRANT SELECT ON oficina_agua_db.* TO 'sync_reader'@'%';
FLUSH PRIVILEGES;
