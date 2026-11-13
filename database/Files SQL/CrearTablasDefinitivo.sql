use medmanager;
 
-- 1. Entidades sin dependencias ---------------------------------------
CREATE TABLE especialidad (
    id_especialidad     INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    nombre_especialidad VARCHAR(50) NOT NULL,
    descripcion         VARCHAR(255)
);
 
CREATE TABLE paciente (
    id_paciente      INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    dni              VARCHAR(9) UNIQUE NOT NULL,
    nombre           VARCHAR(50) NOT NULL,
    apellidos        VARCHAR(100) NOT NULL,
    telefono         VARCHAR(15),
    email            VARCHAR(100),
    direccion        VARCHAR(255),
    fecha_nacimiento DATE,
    alergias         VARCHAR(255),
    activo           TINYINT(1) NOT NULL DEFAULT 1   -- baja lógica (RN5)
);
 
-- 2. Trabajador --------------------------------------------------------
-- La FK no lleva ON DELETE SET NULL: MySQL no permite combinarlo con el CHECK.
-- Así no se puede borrar una especialidad que tenga médicos asignados.
CREATE TABLE trabajador (
    id_trabajador   INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    dni             VARCHAR(9) UNIQUE NOT NULL,
    nombre          VARCHAR(50) NOT NULL,
    apellidos       VARCHAR(100) NOT NULL,
    telefono        VARCHAR(15),
    rol             ENUM('RECEPCIONISTA','ENFERMERO','MEDICO') NOT NULL,
    usuario         VARCHAR(50) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,              -- hash BCrypt
    id_especialidad INT,
    CONSTRAINT fk_trabajador_especialidad
        FOREIGN KEY (id_especialidad) REFERENCES especialidad(id_especialidad),
    -- Especialidad obligatoria solo para MEDICO y nula para el resto
    CONSTRAINT chk_trabajador_especialidad
        CHECK ( (rol = 'MEDICO' AND id_especialidad IS NOT NULL)
             OR (rol <> 'MEDICO' AND id_especialidad IS NULL) )
);
 
-- 3. Entidades transaccionales -----------------------------------------
CREATE TABLE cita (
    id_cita                  INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    id_paciente              INT NOT NULL,
    id_trabajador            INT NOT NULL,              -- médico (lo valida un trigger)
    fecha_hora               DATETIME NOT NULL,
    estado                   VARCHAR(20) NOT NULL,
    motivo_consulta          VARCHAR(255),
    observaciones_enfermeria TEXT NULL,
    -- Vale fecha_hora salvo si la cita está CANCELADA (NULL). Los NULL no
    -- colisionan en un UNIQUE, así que cancelar libera el hueco (RN1, RN2).
    fecha_hora_activa        DATETIME
        GENERATED ALWAYS AS (CASE WHEN estado <> 'CANCELADA' THEN fecha_hora ELSE NULL END) STORED,
    FOREIGN KEY (id_paciente)   REFERENCES paciente(id_paciente),
    FOREIGN KEY (id_trabajador) REFERENCES trabajador(id_trabajador),
    CONSTRAINT chk_cita_estado
        CHECK (estado IN ('PENDIENTE', 'REALIZADA', 'CANCELADA')),
    CONSTRAINT chk_cita_franja                          -- franjas de 15 minutos
        CHECK (MINUTE(fecha_hora) % 15 = 0 AND SECOND(fecha_hora) = 0),
    CONSTRAINT uq_cita_medico_franja   UNIQUE (id_trabajador, fecha_hora_activa),
    CONSTRAINT uq_cita_paciente_franja UNIQUE (id_paciente,   fecha_hora_activa)
);
 
CREATE TABLE historial_medico (
    id_historial   INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    id_paciente    INT NOT NULL,
    id_cita        INT NOT NULL,
    diagnostico    TEXT NOT NULL,
    tratamiento    TEXT,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_historial_cita UNIQUE (id_cita),      -- una cita, como máximo un informe
    FOREIGN KEY (id_paciente) REFERENCES paciente(id_paciente),
    FOREIGN KEY (id_cita)     REFERENCES cita(id_cita)
);