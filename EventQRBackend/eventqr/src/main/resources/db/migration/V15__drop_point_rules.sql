-- Guard: refuse to drop if any rows exist (safety check)
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM point_rules LIMIT 1) THEN
        RAISE EXCEPTION 'point_rules table is not empty — aborting drop';
    END IF;
END $$;

DROP TABLE IF EXISTS point_rules;
