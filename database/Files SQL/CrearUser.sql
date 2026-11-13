CREATE USER 'medmanager_app'@'localhost' IDENTIFIED BY 'PON_AQUI_TU_CLAVE';
GRANT SELECT, INSERT, UPDATE ON medmanager.* TO 'medmanager_app'@'localhost';