CREATE DATABASE IF NOT EXISTS sistema_planilla;
USE sistema_planilla;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS PLANILLA_DESCUENTO;
DROP TABLE IF EXISTS PLANILLA;
DROP TABLE IF EXISTS HORAS_CLASE;
DROP TABLE IF EXISTS SALARIO_BASE;
DROP TABLE IF EXISTS USUARIO;
DROP TABLE IF EXISTS DESCUENTO;
DROP TABLE IF EXISTS EMPLEADO;
DROP TABLE IF EXISTS ROL;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE ROL (
    id_rol BIGINT NOT NULL AUTO_INCREMENT,
    nombre_rol VARCHAR(50) NOT NULL,
    descripcion VARCHAR(150),
    PRIMARY KEY (id_rol),
    UNIQUE KEY uk_rol_nombre (nombre_rol)
);

CREATE TABLE EMPLEADO (
    id_empleado BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(80) NOT NULL,
    apellido VARCHAR(80) NOT NULL,
    identificacion VARCHAR(25) NOT NULL,
    direccion VARCHAR(200),
    tipo VARCHAR(50) NOT NULL,
    salario_base_vigente DOUBLE NOT NULL,
    PRIMARY KEY (id_empleado),
    UNIQUE KEY uk_empleado_identificacion (identificacion)
);

CREATE TABLE DESCUENTO (
    id_descuento BIGINT NOT NULL AUTO_INCREMENT,
    tipo VARCHAR(50) NOT NULL,
    porcentaje DOUBLE NOT NULL,
    vigencia DATE NOT NULL,
    PRIMARY KEY (id_descuento),
    UNIQUE KEY uk_descuento_tipo (tipo)
);

CREATE TABLE SALARIO_BASE (
    id_salario_base BIGINT NOT NULL AUTO_INCREMENT,
    id_empleado BIGINT NOT NULL,
    salario DOUBLE NOT NULL,
    fecha_vigencia DATE NOT NULL,
    PRIMARY KEY (id_salario_base),
    CONSTRAINT fk_salario_empleado FOREIGN KEY (id_empleado) REFERENCES EMPLEADO (id_empleado)
);

CREATE TABLE HORAS_CLASE (
    id_horas BIGINT NOT NULL AUTO_INCREMENT,
    id_empleado BIGINT NOT NULL,
    periodo VARCHAR(20) NOT NULL,
    horas_trabajadas DOUBLE NOT NULL,
    PRIMARY KEY (id_horas),
    UNIQUE KEY uk_horas_empleado_periodo (id_empleado, periodo),
    CONSTRAINT fk_horas_empleado FOREIGN KEY (id_empleado) REFERENCES EMPLEADO (id_empleado)
);

CREATE TABLE PLANILLA (
    id_planilla BIGINT NOT NULL AUTO_INCREMENT,
    id_empleado BIGINT NOT NULL,
    periodo VARCHAR(20) NOT NULL,
    fecha_generacion DATE NOT NULL,
    horas_trabajadas DOUBLE NOT NULL,
    bonificacion DOUBLE NOT NULL,
    salario_bruto DOUBLE NOT NULL,
    total_descuentos DOUBLE NOT NULL,
    salario_neto DOUBLE NOT NULL,
    PRIMARY KEY (id_planilla),
    UNIQUE KEY uk_planilla_empleado_periodo (id_empleado, periodo),
    CONSTRAINT fk_planilla_empleado FOREIGN KEY (id_empleado) REFERENCES EMPLEADO (id_empleado)
);

CREATE TABLE PLANILLA_DESCUENTO (
    id_planilla_descuento BIGINT NOT NULL AUTO_INCREMENT,
    id_planilla BIGINT NOT NULL,
    id_descuento BIGINT NOT NULL,
    monto_descuento DOUBLE NOT NULL,
    PRIMARY KEY (id_planilla_descuento),
    CONSTRAINT fk_planilla_descuento_planilla FOREIGN KEY (id_planilla) REFERENCES PLANILLA (id_planilla),
    CONSTRAINT fk_planilla_descuento_descuento FOREIGN KEY (id_descuento) REFERENCES DESCUENTO (id_descuento)
);

CREATE TABLE USUARIO (
    id_usuario BIGINT NOT NULL AUTO_INCREMENT,
    usuario VARCHAR(50) NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    id_rol BIGINT NOT NULL,
    id_empleado BIGINT NULL,
    PRIMARY KEY (id_usuario),
    UNIQUE KEY uk_usuario_nombre (usuario),
    UNIQUE KEY uk_usuario_empleado (id_empleado),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES ROL (id_rol),
    CONSTRAINT fk_usuario_empleado FOREIGN KEY (id_empleado) REFERENCES EMPLEADO (id_empleado)
);

INSERT INTO ROL (nombre_rol, descripcion) VALUES
('ROLE_ADMIN', 'Administrador general del sistema'),
('ROLE_RRHH', 'Gestor de planillas y reportes'),
('ROLE_EMPLEADO', 'Empleado con acceso a sus boletas');

INSERT INTO EMPLEADO (nombre, apellido, identificacion, direccion, tipo, salario_base_vigente) VALUES
('Andrea', 'Mendez', '01234567-8', 'San Salvador, El Salvador', 'DOCENTE', 720.00),
('Carlos', 'Lopez', '12345678-9', 'Santa Tecla, El Salvador', 'ADMINISTRATIVO', 680.00),
('Lucia', 'Rivas', '23456789-0', 'San Miguel, El Salvador', 'DOCENTE', 760.00);

INSERT INTO DESCUENTO (tipo, porcentaje, vigencia) VALUES
('ISSS', 3.00, CURRENT_DATE()),
('AFP', 7.25, CURRENT_DATE()),
('RENTA', 0.00, CURRENT_DATE());
