CREATE TABLE IF NOT EXISTS users
(
    id            SERIAL PRIMARY KEY,
    username      VARCHAR(64)  NOT NULL UNIQUE,
    password_hash VARCHAR(250) NOT NULL
);