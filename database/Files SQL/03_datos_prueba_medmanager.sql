-- =====================================================================
-- MedManager · Script 03: datos de prueba
-- Ejecutar UNA SOLA VEZ, con la conexión de root, y solo después de crear
-- las tablas y los triggers. No usa id escritos a mano: busca cada dato
-- por su nombre, DNI o usuario, así que funciona aunque el AUTO_INCREMENT
-- no empiece en 1.
--
-- Contraseñas de desarrollo (hash BCrypt $2a$, coste 10):
--   recepcion1 / Recep123!     medico1 y medico2 / Medico123!     enfermero1 / Enfer123!
-- =====================================================================
USE medmanager;

INSERT INTO especialidad (nombre_especialidad, descripcion) VALUES
  ('Medicina General', 'Atención primaria y derivaciones'),
  ('Pediatría',        'Atención a menores de 14 años');

INSERT INTO trabajador (dni, nombre, apellidos, telefono, rol, usuario, password_hash, id_especialidad) VALUES
  ('11111111A', 'Laura',  'Gómez Ruiz',  '600111111', 'RECEPCIONISTA', 'recepcion1',
     '$2a$10$OynrSNVyP5WXcgj6Db3fA.tkTDja2wSXghGqy795lpJO4n67AKg86', NULL),
  ('22222222B', 'Carlos', 'Martín Pons', '600222222', 'MEDICO', 'medico1',
     '$2a$10$gNma7jjFJ6de0HO2q9Dcv.diAyF5vv2rG3impagzSzT2Vl.tMeUzu',
     (SELECT id_especialidad FROM especialidad WHERE nombre_especialidad = 'Medicina General')),
  ('33333333C', 'Marta',  'Vidal Serra', '600333333', 'MEDICO', 'medico2',
     '$2a$10$OLtk8JQhOix5V.NzjmSbfOKDz8Po0OvSpreL7f0yiJjWmV1Fu7qWG',
     (SELECT id_especialidad FROM especialidad WHERE nombre_especialidad = 'Pediatría')),
  ('44444444D', 'Jordi',  'Casals Roca', '600444444', 'ENFERMERO', 'enfermero1',
     '$2a$10$sLN37e22ZQaGoRsXGMBQJOGihTLjv3wPbdwqCBgk3ssinBiqYYkWG', NULL);

INSERT INTO paciente (dni, nombre, apellidos, telefono, email, direccion, fecha_nacimiento, alergias, activo) VALUES
  ('55555555E', 'Ana',   'Pérez López', '611555555', 'ana.perez@example.com',  'C/ Major 12, Sabadell', '1985-03-14', 'Penicilina', 1),
  ('66666666F', 'Pedro', 'Sanz Molina', '611666666', 'pedro.sanz@example.com', 'Av. Roma 5, Sabadell',  '1972-11-02', NULL,         1);

-- Dos citas con el médico medico1. Fechas: cámbialas si quieres otras
-- (minutos siempre 00, 15, 30 o 45).
INSERT INTO cita (id_paciente, id_trabajador, fecha_hora, estado, motivo_consulta)
SELECT p.id_paciente, t.id_trabajador, TIMESTAMP(CURDATE(), '12:00:00'), 'PENDIENTE', 'Revisión anual'
FROM paciente p, trabajador t WHERE p.dni = '55555555E' AND t.usuario = 'medico1';

INSERT INTO cita (id_paciente, id_trabajador, fecha_hora, estado, motivo_consulta)
SELECT p.id_paciente, t.id_trabajador, TIMESTAMP(CURDATE(), '19:30:00'), 'PENDIENTE', 'Control pediátrico'
FROM paciente p, trabajador t WHERE p.dni = '66666666F' AND t.usuario = 'medico2';

-- Comprobación
SELECT id_trabajador, usuario, rol, id_especialidad FROM trabajador;
SELECT id_cita, id_paciente, id_trabajador, fecha_hora, estado FROM cita;
