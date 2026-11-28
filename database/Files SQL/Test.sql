-- =====================================================================
-- MedManager · Pruebas de base de datos (memoria, apartado 8: PBD-01 a PBD-07)
-- Todo va dentro de una transacción y se deshace con ROLLBACK al final:
-- la base de datos queda igual que antes de empezar.
--
-- Antes de empezar: cargar 03_datos_prueba_medmanager.sql (si se han vaciado
-- las tablas con Vaciar_datos.sql, las variables saldrían NULL).
--
-- Ejecutar SENTENCIA A SENTENCIA en Workbench (cursor en la sentencia y
-- Ctrl + Enter) y sacar la captura del Output de cada prueba.
-- No cerrar la pestaña hasta el ROLLBACK: las variables solo existen en ella.
-- =====================================================================

SELECT VERSION();
USE medmanager;
START TRANSACTION;

-- ---------------------------------------------------------------------
-- Variables: se buscan por usuario o por DNI, no por id, para que no
-- dependan del AUTO_INCREMENT.
-- ---------------------------------------------------------------------
SET @med1 = (SELECT id_trabajador FROM trabajador WHERE usuario = 'medico1');
SET @med2 = (SELECT id_trabajador FROM trabajador WHERE usuario = 'medico2');
SET @enf  = (SELECT id_trabajador FROM trabajador WHERE usuario = 'enfermero1');
SET @rec  = (SELECT id_trabajador FROM trabajador WHERE usuario = 'recepcion1');
SET @pac1 = (SELECT id_paciente FROM paciente WHERE dni = '55555555E');  -- Ana Pérez
SET @pac2 = (SELECT id_paciente FROM paciente WHERE dni = '66666666F');  -- Pedro Sanz

-- COMPROBACIÓN: ninguna columna puede salir NULL. Si alguna sale NULL,
-- parar aquí y cargar 03_datos_prueba_medmanager.sql.
SELECT @med1, @med2, @enf, @rec, @pac1, @pac2;

-- ---------------------------------------------------------------------
-- PBD-01 (RN1): franja de 15 minutos
-- Esperado: Error 3819 Check constraint 'chk_cita_franja' is violated
-- ---------------------------------------------------------------------
INSERT INTO cita (id_paciente, id_trabajador, fecha_hora, estado)
VALUES (@pac1, @med1, '2027-01-15 10:07:00', 'PENDIENTE');

-- ---------------------------------------------------------------------
-- PBD-02 (RN1): un médico no puede tener dos citas a la misma hora
-- ---------------------------------------------------------------------
-- Cita correcta de Ana con medico1. Esperado: 1 row(s) affected
INSERT INTO cita (id_paciente, id_trabajador, fecha_hora, estado)
VALUES (@pac1, @med1, '2027-01-15 10:00:00', 'PENDIENTE');

-- Se guarda su id para las pruebas siguientes
SET @cita = LAST_INSERT_ID();
SELECT * FROM cita WHERE id_cita = @cita;

-- Pedro con el mismo médico a la misma hora.
-- Esperado: Error 1062 ... for key 'cita.uq_cita_medico_franja'
INSERT INTO cita (id_paciente, id_trabajador, fecha_hora, estado)
VALUES (@pac2, @med1, '2027-01-15 10:00:00', 'PENDIENTE');

-- ---------------------------------------------------------------------
-- PBD-02b (RN2): un paciente no puede tener dos citas a la misma hora
-- Ana ya tiene la cita de las 10:00 con medico1; ahora con medico2.
-- Esperado: Error 1062 ... for key 'cita.uq_cita_paciente_franja'
-- ---------------------------------------------------------------------
INSERT INTO cita (id_paciente, id_trabajador, fecha_hora, estado)
VALUES (@pac1, @med2, '2027-01-15 10:00:00', 'PENDIENTE');

-- ---------------------------------------------------------------------
-- PBD-03 (RN1): cancelar una cita libera el hueco
-- ---------------------------------------------------------------------
-- Se cancela la cita de Ana. Esperado: 1 row(s) affected
UPDATE cita SET estado = 'CANCELADA' WHERE id_cita = @cita;

-- Esperado: estado CANCELADA y fecha_hora_activa NULL
SELECT id_cita, estado, fecha_hora, fecha_hora_activa FROM cita WHERE id_cita = @cita;

-- Se repite la cita de Pedro que falló en PBD-02. Esperado: 1 row(s) affected
INSERT INTO cita (id_paciente, id_trabajador, fecha_hora, estado)
VALUES (@pac2, @med1, '2027-01-15 10:00:00', 'PENDIENTE');

-- ---------------------------------------------------------------------
-- PBD-04a: una cita solo se puede crear para un médico (trigger trg_cita_bi)
-- Esperado en las dos: Error 1644 La cita solo puede asignarse a un
-- trabajador con rol MEDICO
-- ---------------------------------------------------------------------
INSERT INTO cita (id_paciente, id_trabajador, fecha_hora, estado)
VALUES (@pac1, @enf, '2027-01-15 11:00:00', 'PENDIENTE');

INSERT INTO cita (id_paciente, id_trabajador, fecha_hora, estado)
VALUES (@pac1, @rec, '2027-01-15 11:00:00', 'PENDIENTE');

-- ---------------------------------------------------------------------
-- PBD-04b: una cita existente no se puede pasar a quien no sea médico
-- (trigger trg_cita_bu). Esperado: Error 1644, mismo mensaje
-- ---------------------------------------------------------------------
UPDATE cita SET id_trabajador = @enf WHERE id_cita = @cita;

-- ---------------------------------------------------------------------
-- PBD-05: el informe tiene que ser del paciente de la cita
-- (trigger trg_historial_bi). @cita es de Ana (@pac1).
-- ---------------------------------------------------------------------
-- Informe a nombre de Pedro. Esperado: Error 1644 El paciente del informe
-- no coincide con el paciente de la cita
INSERT INTO historial_medico (id_paciente, id_cita, diagnostico)
VALUES (@pac2, @cita, 'Prueba de trigger');

-- Informe correcto, a nombre de Ana. Esperado: 1 row(s) affected
INSERT INTO historial_medico (id_paciente, id_cita, diagnostico)
VALUES (@pac1, @cita, 'Prueba de trigger');

-- Segundo informe de la misma cita.
-- Esperado: Error 1062 ... for key 'historial_medico.uq_historial_cita'
INSERT INTO historial_medico (id_paciente, id_cita, diagnostico)
VALUES (@pac1, @cita, 'Segundo informe');

-- ---------------------------------------------------------------------
-- PBD-06: especialidad solo para médicos
-- Esperado en las dos: Error 3819 Check constraint
-- 'chk_trabajador_especialidad' is violated
-- ---------------------------------------------------------------------
-- Médico sin especialidad
INSERT INTO trabajador (dni, nombre, apellidos, rol, usuario, password_hash, id_especialidad)
VALUES ('99999999Z', 'Prueba', 'Sin Especialidad', 'MEDICO', 'prueba_medico', 'x', NULL);

-- Recepcionista con especialidad
INSERT INTO trabajador (dni, nombre, apellidos, rol, usuario, password_hash, id_especialidad)
VALUES ('99999998Y', 'Prueba', 'Con Especialidad', 'RECEPCIONISTA', 'prueba_recep', 'x',
        (SELECT MIN(id_especialidad) FROM especialidad));

-- ---------------------------------------------------------------------
-- Fin: se deshacen todos los datos insertados en las pruebas
-- ---------------------------------------------------------------------
ROLLBACK;

-- ---------------------------------------------------------------------
-- PBD-07 (RN5): el usuario de la aplicación no puede borrar.
-- OJO: se ejecuta en OTRA conexión de Workbench, con el usuario
-- medmanager_app (nunca con root, que sí borraría).
-- Esperado: Error 1142 DELETE command denied to user
-- 'medmanager_app'@'localhost' for table 'cita'
-- ---------------------------------------------------------------------
-- DELETE FROM medmanager.cita WHERE id_cita = -1;
