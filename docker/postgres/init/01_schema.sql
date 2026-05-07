-- Schema for users table matching JPA entity
CREATE TABLE IF NOT EXISTS users (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255),
  profession VARCHAR(255),
  income_bracket VARCHAR(255),
  postal_code VARCHAR(32),
  access_class VARCHAR(50)
);
