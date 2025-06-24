CREATE TABLE IF NOT EXISTS game_results
(
    id            SERIAL PRIMARY KEY,
    score         INTEGER                       NOT NULL,
    time_finished TIMESTAMP                     NOT NULL,
    creator_id    INTEGER REFERENCES users (id) NOT NULL,
    other_user_id INTEGER REFERENCES users (id) NOT NULL
);