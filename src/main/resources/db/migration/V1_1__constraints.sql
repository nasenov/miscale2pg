ALTER TABLE measurement
    ADD CONSTRAINT measurement_weight_check
        CHECK (5 <= weight AND weight <= 150);

ALTER TABLE measurement
    ADD CONSTRAINT measurement_height_check
        CHECK (90 <= height AND height <= 220);

ALTER TABLE measurement
    ADD CONSTRAINT measurement_bmi_check
        CHECK (abs(bmi - (weight / power(height / 100, 2))) <= 0.1);

ALTER TABLE measurement
    ADD CONSTRAINT measurement_fat_rate_check
        CHECK (5 <= fat_rate AND fat_rate <= 50);

ALTER TABLE measurement
    ADD CONSTRAINT measurement_body_water_rate_check
        CHECK (40 <= body_water_rate AND body_water_rate <= 80);

ALTER TABLE measurement
    ADD CONSTRAINT measurement_bone_mass_check
        CHECK (0 < bone_mass AND bone_mass < 20 AND bone_mass < weight);

ALTER TABLE measurement
    ADD CONSTRAINT measurement_metabolism_check
        CHECK (1000 <= metabolism AND metabolism <= 3000);

ALTER TABLE measurement
    ADD CONSTRAINT measurement_muscle_rate_check
        CHECK (0 < muscle_rate AND muscle_rate < weight);

ALTER TABLE measurement
    ADD CONSTRAINT measurement_visceral_fat_check
        CHECK (0 <= visceral_fat AND visceral_fat <= 30);

ALTER TABLE measurement
    ADD CONSTRAINT measurement_body_composition_check
        CHECK (abs(weight - (fat_rate / 100 * weight) - bone_mass - muscle_rate) <= 1);
