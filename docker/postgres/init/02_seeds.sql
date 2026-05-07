-- Seeds with realistic examples for testing
-- VIP example: Médico com renda alta e CEP em área nobre
INSERT INTO users (name, profession, income_bracket, postal_code, access_class)
VALUES ('Dra. Maria Silva', 'doctor', '>10000', '01311-000', 'VIP');

-- STANDARD example: Engenheiro com renda média
INSERT INTO users (name, profession, income_bracket, postal_code, access_class)
VALUES ('Carlos Pereira', 'engineer', '5000-10000', '20040-010', 'STANDARD');

-- BASE example: Estudante com baixa renda
INSERT INTO users (name, profession, income_bracket, postal_code, access_class)
VALUES ('João Santos', 'student', '<2000', '57000-000', 'BASE');

-- Additional varied examples
INSERT INTO users (name, profession, income_bracket, postal_code, access_class)
VALUES ('Ana Oliveira', 'nurse', '2000-5000', '31030-010', 'STANDARD');

INSERT INTO users (name, profession, income_bracket, postal_code, access_class)
VALUES ('Pedro Costa', 'teacher', '<2000', '40020-000', 'BASE');
