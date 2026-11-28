use medmanager;
-- Estados permitidos de la cita
ALTER TABLE cita
  ADD CONSTRAINT chk_cita_estado
  CHECK (estado IN ('PENDIENTE', 'REALIZADA', 'CANCELADA'));
-- Franjas de 15 minutos (RN1): minutos 00/15/30/45 y segundos 0
ALTER TABLE cita
  ADD CONSTRAINT chk_cita_franja
  CHECK (MINUTE(fecha_hora) % 15 = 0 AND SECOND(fecha_hora) = 0);
-- Especialidad obligatoria solo para MEDICO y nula para el resto--
ALTER TABLE trabajador
  ADD CONSTRAINT chk_trabajador_especialidad
  CHECK ( (rol = 'MEDICO' AND id_especialidad IS NOT NULL)
       OR (rol <> 'MEDICO' AND id_especialidad IS NULL) );