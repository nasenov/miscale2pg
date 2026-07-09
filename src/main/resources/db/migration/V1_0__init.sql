CREATE TABLE measurement
(
    "time"          timestamptz   NOT NULL,
    weight          numeric(5, 2) NOT NULL,
    height          numeric(4, 1) NOT NULL,
    bmi             numeric(3, 1) NOT NULL,
    fat_rate        numeric(4, 2) NULL,
    body_water_rate numeric(4, 2) NULL,
    bone_mass       numeric(4, 2) NULL,
    metabolism      numeric(5, 1) NULL,
    muscle_rate     numeric(4, 2) NULL,
    visceral_fat    numeric(3, 1) NULL,
    CONSTRAINT measurement_pk PRIMARY KEY ("time")
);
