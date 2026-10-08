use Medmanager;
-- 1. Entidades Principales (Sin dependencias)
CREATE TABLE Especialidad (
    id_especialidad INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    nombre_especialidad VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255)
);

CREATE TABLE Paciente (
    id_paciente INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    dni VARCHAR(9) UNIQUE NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    telefono VARCHAR(15),
    email VARCHAR(100),
    direccion VARCHAR(255),
    fecha_nacimiento DATE,
    activo boolean default true
);

-- 2. Entidad Secundaria (Trabajador con el ENUM aplicado)
CREATE TABLE Trabajador (
    id_trabajador INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    dni VARCHAR(9) UNIQUE NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    telefono VARCHAR(15),
    rol ENUM('Recepcionista', 'Enfermero', 'Médico') NOT NULL, 
    usuario VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    id_especialidad INT, 
    FOREIGN KEY (id_especialidad) REFERENCES Especialidad(id_especialidad) ON DELETE SET NULL
);

-- 3. Entidades Transaccionales (El núcleo de la clínica)
CREATE TABLE Cita (
    id_cita INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    id_paciente INT NOT NULL,
    id_trabajador INT NOT NULL,
    fecha_hora DATETIME NOT NULL,
    estado VARCHAR(20) NOT NULL, -- Pendiente, Realizada, Cancelada
    motivo_consulta VARCHAR(255),
    diagnostico TEXT,
    FOREIGN KEY (id_paciente) REFERENCES Paciente(id_paciente),
    FOREIGN KEY (id_trabajador) REFERENCES Trabajador(id_trabajador)
);

CREATE TABLE Historial_Medico (
    id_historial INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    id_paciente INT NOT NULL,
    id_cita INT NOT NULL,
    diagnostico TEXT NOT NULL,
    tratamiento TEXT,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_paciente) REFERENCES Paciente(id_paciente),
    FOREIGN KEY (id_cita) REFERENCES Cita(id_cita)
);