DROP TABLE IF EXISTS sugars;

CREATE TABLE IF NOT EXISTS sugars (
id SERIAL PRIMARY KEY,
levelSugar FLOAT NOT NULL,
doseOfInsulin FLOAT DEFAULT 0,
time TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
note VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS sugars_levelSugar_idx ON sugars (levelSugar);
CREATE INDEX IF NOT EXISTS sugars_doseOfInsulin_idx ON sugars (doseOfInsulin);
CREATE INDEX IF NOT EXISTS sugars_time_idx ON sugars (time);