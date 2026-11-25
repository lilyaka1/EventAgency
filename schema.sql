CREATE TABLE clients (
    id SERIAL PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL UNIQUE,
    phone VARCHAR(30) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE event_orders (
    id SERIAL PRIMARY KEY,
    client_id INTEGER NOT NULL REFERENCES clients(id) ON DELETE RESTRICT,
    title VARCHAR(160) NOT NULL,
    event_type VARCHAR(30) NOT NULL CHECK (event_type IN ('WEDDING', 'CONFERENCE', 'BIRTHDAY', 'CORPORATE', 'OTHER')),
    status VARCHAR(30) NOT NULL CHECK (status IN ('PLANNED', 'CONFIRMED', 'COMPLETED', 'CANCELLED')),
    event_date DATE NOT NULL,
    budget NUMERIC(12,2) NOT NULL CHECK (budget > 0),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO clients(full_name, email, phone) VALUES
('Анна Смирнова', 'anna@example.com', '+79990000001'),
('Иван Петров', 'ivan@example.com', '+79990000002'),
('Мария Волкова', 'maria@example.com', '+79990000003'),
('Дмитрий Орлов', 'dmitry@example.com', '+79990000004'),
('Елена Соколова', 'elena@example.com', '+79990000005');

INSERT INTO event_orders(client_id, title, event_type, status, event_date, budget, description) VALUES
(1, 'Свадьба Анны', 'WEDDING', 'CONFIRMED', '2026-06-12', 250000, 'Полное оформление площадки'),
(2, 'IT-конференция', 'CONFERENCE', 'PLANNED', '2026-07-03', 480000, 'Сцена и техническое сопровождение'),
(3, 'День рождения', 'BIRTHDAY', 'COMPLETED', '2026-02-14', 90000, 'Вечеринка на 30 гостей'),
(4, 'Корпоратив компании', 'CORPORATE', 'CONFIRMED', '2026-09-18', 320000, 'Фуршет и ведущий'),
(5, 'Юбилей', 'OTHER', 'CANCELLED', '2026-04-20', 150000, 'Мероприятие отменено клиентом'),
(1, 'Свадьба Ирины', 'WEDDING', 'PLANNED', '2026-10-10', 310000, 'Декор и координация'),
(2, 'Презентация продукта', 'CORPORATE', 'CONFIRMED', '2026-11-05', 210000, 'Зал и оборудование'),
(3, 'Детский праздник', 'BIRTHDAY', 'PLANNED', '2026-12-12', 75000, 'Аниматоры и кенди-бар'),
(4, 'Форум партнеров', 'CONFERENCE', 'PLANNED', '2027-01-20', 560000, 'Регистрация и кейтеринг'),
(5, 'Новогодний вечер', 'CORPORATE', 'CONFIRMED', '2026-12-25', 430000, 'Шоу-программа и оформление');
