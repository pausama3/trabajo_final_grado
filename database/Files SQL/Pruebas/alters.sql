use Medmanager;
ALTER TABLE cita CHANGE observaciones observaciones_enfermeria TEXT NULL;
ALTER TABLE cita DROP COLUMN diagnostico;

ALTER TABLE paciente ADD COLUMN alergias VARCHAR(255) NULL AFTER fecha_nacimiento;
ALTER TABLE paciente MODIFY activo TINYINT(1) NOT NULL DEFAULT 1;

ALTER TABLE historial_medico ADD CONSTRAINT uq_historial_cita UNIQUE (id_cita);

ALTER TABLE trabajador MODIFY rol ENUM('RECEPCIONISTA','ENFERMERO','MEDICO') NOT NULL;