CREATE TABLE IF NOT EXISTS line_origin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    origin_number BIGINT NOT NULL UNIQUE,
    description VARCHAR(255)
);

INSERT INTO line_origin (origin_number, description) VALUES
('12345', 'Línea de origen 1'),
('23456', 'Línea de origen 2'),
('34567', 'Línea de origen 3'),
('45678', 'Línea de origen 4'),
('56789', 'Línea de origen 5');