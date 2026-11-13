use medmanager;
DROP TRIGGER IF EXISTS trg_cita_bi;

DELIMITER $$
CREATE TRIGGER trg_cita_bi BEFORE INSERT ON cita
FOR EACH ROW
BEGIN
  DECLARE v_rol VARCHAR(20);
  SELECT rol INTO v_rol FROM trabajador WHERE id_trabajador = NEW.id_trabajador;
  IF v_rol IS NULL OR v_rol <> 'MEDICO' THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'La cita solo puede asignarse a un trabajador con rol MEDICO';
  END IF;
END$$
DELIMITER ;