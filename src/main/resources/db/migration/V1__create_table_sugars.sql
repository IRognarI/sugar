CREATE TABLE IF NOT EXISTS sugars
(
    id
    SERIAL
    PRIMARY
    KEY,
    level_sugar
    FLOAT
    NOT
    NULL,
    dose_of_insulin
    FLOAT
    DEFAULT
    0,
    time
    TIMESTAMP
    WITH
    TIME
    ZONE
    NOT
    NULL,
    note
    VARCHAR
(
    255
)
    );

CREATE INDEX IF NOT EXISTS sugars_levelSugar_idx ON sugars (level_sugar);
CREATE INDEX IF NOT EXISTS sugars_doseOfInsulin_idx ON sugars (dose_of_insulin);
CREATE INDEX IF NOT EXISTS sugars_time_idx ON sugars (time);