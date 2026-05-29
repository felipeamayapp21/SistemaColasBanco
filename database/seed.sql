-- ============================================================
--  ProyectoBancario — Seed: datos de ejemplo
--  Ejecutar tras schema.sql
-- ============================================================

TRUNCATE historial_atencion, clientes, usuarios RESTART IDENTITY CASCADE;

-- Usuarios
INSERT INTO usuarios (username, password, rol, tipo_recuperacion, pregunta_seguridad, respuesta_seguridad) VALUES
    ('admin',   'admin123', 'ADMIN',  'PREGUNTA', '¿Cuál es su contraseña inicial del sistema?', 'admin123'),
    ('cajero1', 'admin123', 'CAJERO', 'PREGUNTA', '¿Cuál es su contraseña inicial del sistema?', 'admin123'),
    ('cajero2', 'admin123', 'CAJERO', 'PALABRA_CLAVE', 'Indique su palabra clave de recuperación', 'banco2025'),
    ('cajero3', 'admin123', 'CAJERO', 'PREGUNTA', '¿En qué ciudad nació?', 'bogota');

-- Clientes en ESPERA (con tickets)
INSERT INTO clientes (ticket, nombre, documento, edad, genero, direccion, telefono, correo, tipo_cliente, prioridad, hora_ingreso, estado) VALUES
    ('G001', 'Maria Garcia',    '1000000001', 32, 'Femenino', 'Calle 1 #10-20', '3001110001', 'maria@mail.com', 'REGULAR', FALSE, NOW() - INTERVAL '214 minutes', 'ESPERA'),
    ('G002', 'Carlos Perez',    '1000000002', 53, 'Masculino', 'Carrera 5 #20-30', '3001110002', 'carlos@mail.com', 'REGULAR', FALSE, NOW() - INTERVAL '201 minutes', 'ESPERA'),
    ('G003', 'Ana Martinez',    '1000000003', 46, 'Femenino', 'Av Siempre Viva 742', '3001110003', 'ana@mail.com', 'REGULAR', FALSE, NOW() - INTERVAL '115 minutes', 'ESPERA'),
    ('G004', 'Luis Rodriguez',  '1000000004', 31, 'Masculino', 'Calle 10 #5-30', '3001110004', 'luis@mail.com', 'REGULAR', FALSE, NOW() - INTERVAL '72 minutes', 'ESPERA'),
    ('P001', 'Rosa Jimenez',    '1000000005', 72, 'Femenino', 'Cra 8 #12-45', '3001110005', 'rosa@mail.com', 'PRIORITARIO', TRUE, NOW() - INTERVAL '270 minutes', 'ESPERA'),
    ('G005', 'Pedro Lopez',     '1000000006', 21, 'Masculino', 'Calle 50 #30-10', '3001110006', 'pedro@mail.com', 'REGULAR', FALSE, NOW() - INTERVAL '77 minutes', 'ESPERA'),
    ('G006', 'Laura Hernandez', '1000000007', 45, 'Femenino', 'Av 68 #20-30', '3001110007', 'laura@mail.com', 'REGULAR', FALSE, NOW() - INTERVAL '191 minutes', 'ESPERA'),
    ('P002', 'Jorge Diaz',      '1000000008', 82, 'Masculino', 'Calle 100 #15-40', '3001110008', 'jorge@mail.com', 'PRIORITARIO', TRUE, NOW() - INTERVAL '227 minutes', 'ESPERA'),
    ('G007', 'Carmen Moreno',   '1000000009', 43, 'Femenino', 'Cra 7 #60-20', '3001110009', 'carmen@mail.com', 'REGULAR', FALSE, NOW() - INTERVAL '344 minutes', 'ESPERA'),
    ('G008', 'Andres Alvarez',  '1000000010', 46, 'Masculino', 'Calle 26 #13-50', '3001110010', 'andres@mail.com', 'REGULAR', FALSE, NOW() - INTERVAL '368 minutes', 'ESPERA');

-- Clientes ATENDIDOS (con historial)
INSERT INTO clientes (ticket, nombre, documento, edad, genero, direccion, telefono, correo, tipo_cliente, prioridad, hora_ingreso, estado) VALUES
    ('G009', 'Patricia Romero', '1000000011', 53, 'Femenino', 'Calle 3 #5-60', '3001110011', 'patricia@mail.com', 'REGULAR', FALSE, NOW() - INTERVAL '400 minutes', 'ATENDIDO'),
    ('P003', 'Diego Torres',    '1000000012', 67, 'Masculino', 'Cra 11 #8-90', '3001110012', 'diego@mail.com', 'PRIORITARIO', TRUE, NOW() - INTERVAL '350 minutes', 'ATENDIDO'),
    ('G010', 'Sofia Ruiz',      '1000000013', 38, 'Femenino', 'Av Caracas #12-30', '3001110013', 'sofia@mail.com', 'REGULAR', FALSE, NOW() - INTERVAL '228 minutes', 'ATENDIDO'),
    ('P004', 'Miguel Vargas',   '1000000014', 75, 'Masculino', 'Calle 80 #20-10', '3001110014', 'miguel@mail.com', 'PRIORITARIO', TRUE, NOW() - INTERVAL '177 minutes', 'ATENDIDO'),
    ('G011', 'Elena Castro',    '1000000015', 29, 'Femenino', 'Cra 50 #40-20', '3001110015', 'elena@mail.com', 'REGULAR', FALSE, NOW() - INTERVAL '84 minutes', 'ATENDIDO');

-- Historial para clientes ATENDIDOS
INSERT INTO historial_atencion (cliente_id, ticket, hora_atencion, hora_fin, tiempo_espera, duracion, cajero_usuario, motivo_visita, tipo_tramite, estado_atencion, observaciones)
SELECT
    c.id,
    c.ticket,
    c.hora_ingreso + INTERVAL '1 minute' * (ROW_NUMBER() OVER (ORDER BY c.id) * 5),
    c.hora_ingreso + INTERVAL '1 minute' * (ROW_NUMBER() OVER (ORDER BY c.id) * 5 + 10),
    (ROW_NUMBER() OVER (ORDER BY c.id) * 5) * 60,
    (10 + ROW_NUMBER() OVER (ORDER BY c.id) * 2) * 60,
    CASE (c.id % 3) WHEN 0 THEN 'admin' WHEN 1 THEN 'cajero1' ELSE 'cajero2' END,
    CASE (c.id % 3) WHEN 0 THEN 'Consulta general' WHEN 1 THEN 'Retiro' ELSE 'Deposito' END,
    CASE (c.id % 3) WHEN 0 THEN 'Asesoria' WHEN 1 THEN 'Caja' ELSE 'Ventanilla' END,
    'COMPLETADO',
    CASE (c.id % 2) WHEN 0 THEN 'Sin novedades' ELSE 'Cliente satisfecho' END
FROM clientes c
WHERE c.estado = 'ATENDIDO';
