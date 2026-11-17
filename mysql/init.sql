-- Se ejecuta solo la primera vez que se crea el volumen de MySQL.
-- Una base de datos por microservicio: cada uno es dueño de sus tablas.
CREATE DATABASE IF NOT EXISTS autofacil_usuarios;
CREATE DATABASE IF NOT EXISTS autofacil_vehiculos;
CREATE DATABASE IF NOT EXISTS autofacil_ventas;
CREATE DATABASE IF NOT EXISTS autofacil_contacto;
GRANT ALL PRIVILEGES ON autofacil_usuarios.* TO 'autofacil'@'%';
GRANT ALL PRIVILEGES ON autofacil_vehiculos.* TO 'autofacil'@'%';
GRANT ALL PRIVILEGES ON autofacil_ventas.* TO 'autofacil'@'%';
GRANT ALL PRIVILEGES ON autofacil_contacto.* TO 'autofacil'@'%';
FLUSH PRIVILEGES;
