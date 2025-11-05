-- limpieza (descomentar)
-- DROP TABLE IF EXISTS Transacciones;
-- DROP TABLE IF EXISTS Prestamos;
-- DROP TABLE IF EXISTS Cuentas;

-- Tablas
CREATE TABLE Cuentas (
    id_cuenta VARCHAR(10) PRIMARY KEY,
    id_cliente VARCHAR(10) NOT NULL,
    saldo NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    fecha_apertura DATE NOT NULL DEFAULT CURRENT_DATE
);
CREATE TABLE Prestamos (
    id_prestamo VARCHAR(10) PRIMARY KEY,
    id_cliente VARCHAR(10) NOT NULL,
    monto_total NUMERIC(10, 2) NOT NULL,
    saldo_pendiente NUMERIC(10, 2) NOT NULL,
    estado VARCHAR(10) NOT NULL CHECK (estado IN ('activo', 'pagado')),
    fecha_solicitud DATE NOT NULL DEFAULT CURRENT_DATE
);
CREATE TABLE Transacciones (
    id_transaccion VARCHAR(10) PRIMARY KEY,
    id_cuenta VARCHAR(10) NOT NULL,
    tipo VARCHAR(10) NOT NULL CHECK (tipo IN ('depósito', 'retiro')),
    monto NUMERIC(10, 2) NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_cuenta) REFERENCES Cuentas (id_cuenta)
);

-- Insertar datos de ejemplo

INSERT INTO Cuentas (id_cuenta, id_cliente, saldo, fecha_apertura) VALUES
('CU001', 'CL001', 2500.00, '2023-01-15'),
('CU002', 'CL002', 1506.50, '2023-08-22'),
('CU003', 'CL003', 980.75, '2023-06-10');

INSERT INTO Prestamos (id_prestamo, id_cliente, monto_total, saldo_pendiente, estado, fecha_solicitud) VALUES
('PR001', 'CL001', 10000.00, 8000.00, 'activo', '2023-01-20'),
('PR002', 'CL002', 5000.00, 2500.00, 'activo', '2023-03-25'),
('PR003', 'CL003', 7500.00, 0.00, 'pagado', '2023-05-10');

INSERT INTO Transacciones (id_transaccion, id_cuenta, tipo, monto, fecha) VALUES
('TR001', 'CU001', 'depósito', 500.00, '2023-02-01 10:00:00'),
('TR002', 'CU002', 'retiro', 200.00, '2023-04-01 14:30:00'),
('TR003', 'CU001', 'depósito', 300.00, '2023-06-15 09:15:00');