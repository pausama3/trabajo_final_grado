use medmanager;
DROP TRIGGER IF EXISTS trg_historial_bi;

DELIMITER $$
CREATE TRIGGER trg_historial_bi BEFORE INSERT ON historial_medico
FOR EACH ROW
BEGIN
  DECLARE v_pac INT;
  SELECT id_paciente INTO v_pac FROM cita WHERE id_cita = NEW.id_cita;
  IF v_pac IS NULL OR v_pac <> NEW.id_paciente THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'El paciente del informe no coincide con el paciente de la cita';
  END IF;
END$$
DELIMITER ;