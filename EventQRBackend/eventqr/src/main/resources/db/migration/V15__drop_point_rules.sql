-- V15: Drop the legacy point_rules table (data moved to transaction_rules in V8/V10).
--
-- IDEMPOTENCY GUARDS:
--   Wrapped entirely in a table-existence check before any DDL.
--   Inside the guard: refuse to drop if any rows exist (safety check).
--   DROP TABLE IF EXISTS for the actual drop (safe if table was already dropped).
--
-- LEGACY-DRIFT SAFE: On a drifted DB where point_rules was already dropped by ddl-auto
-- or manual cleanup, the outer guard finds no table and the entire block is skipped.
-- On a greenfield DB, point_rules exists from V8 but is empty → safety check passes → drop proceeds.

DO $$ BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables
    WHERE table_schema = 'public'
      AND table_name = 'point_rules'
  ) THEN
    IF EXISTS (SELECT 1 FROM public.point_rules LIMIT 1) THEN
      RAISE EXCEPTION 'point_rules table is not empty — aborting drop';
    END IF;

    DROP TABLE IF EXISTS public.point_rules;
  END IF;
END $$;
