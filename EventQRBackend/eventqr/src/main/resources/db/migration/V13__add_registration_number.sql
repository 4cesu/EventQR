-- V13: Per-event attendee registration number (SDD Module 3.7 scope-tracked decision).
-- registration_number is a 1..N sequence PER event_id (not global), assigned by trigger on insert.
--
-- IDEMPOTENCY GUARDS:
--   ADD COLUMN → guarded by information_schema.columns check (Postgres has no ADD COLUMN IF NOT EXISTS)
--   SET NOT NULL → guarded by pg_attribute.attnotnull check
--   ADD CONSTRAINT → guarded by pg_constraint check (matches V18 idiom)
--   CREATE TRIGGER → DROP TRIGGER IF EXISTS before CREATE TRIGGER (matches V17 idiom)
--   CREATE FUNCTION → already CREATE OR REPLACE FUNCTION (natively idempotent)
--   UPDATE backfill → WHERE registration_number IS NULL is already idempotent
--
-- LEGACY-DRIFT SAFE: On a drifted DB where ddl-auto=update already created the column,
-- constraint, function, and trigger, every guard skips its block. Greenfield DBs create
-- everything normally because the guards pass.

BEGIN;

-- 1. Add nullable column first
-- Guard: Postgres has no ADD COLUMN IF NOT EXISTS; use information_schema check.
DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public'
      AND table_name = 'event_registrations'
      AND column_name = 'registration_number'
  ) THEN
    ALTER TABLE public.event_registrations
    ADD COLUMN registration_number integer;
  END IF;
END $$;

-- 2. Backfill existing rows, ordered by created_at (id as tiebreaker)
-- Idempotent: WHERE er.registration_number IS NULL skips already-backfilled rows.
WITH numbered AS (
  SELECT id,
         row_number() OVER (PARTITION BY event_id ORDER BY created_at, id) AS rn
  FROM public.event_registrations
)
UPDATE public.event_registrations er
SET registration_number = numbered.rn
FROM numbered
WHERE er.id = numbered.id
  AND er.registration_number IS NULL;

-- 3. Enforce NOT NULL now that backfill is done
-- Guard: pg_attribute.attnotnull; only alters if NOT NULL not yet set.
DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_attribute
    WHERE attrelid = 'public.event_registrations'::regclass
      AND attname = 'registration_number'
      AND attnotnull
  ) THEN
    ALTER TABLE public.event_registrations
    ALTER COLUMN registration_number SET NOT NULL;
  END IF;
END $$;

-- 4. Uniqueness per event
-- Guard: pg_constraint check (matches V18 idiom).
DO $$ BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM pg_constraint
    WHERE conname = 'uq_event_registrations_event_regnum'
      AND connamespace = 'public'::regnamespace
  ) THEN
    ALTER TABLE public.event_registrations
    ADD CONSTRAINT uq_event_registrations_event_regnum
    UNIQUE (event_id, registration_number);
  END IF;
END $$;

-- 5. Trigger function: assign next number on insert (max()+1 per event_id, gaps OK).
-- Already CREATE OR REPLACE FUNCTION → natively idempotent, no guard needed.
CREATE OR REPLACE FUNCTION public.assign_registration_number()
RETURNS trigger AS $$
BEGIN
  IF NEW.registration_number IS NULL THEN
    SELECT COALESCE(MAX(registration_number), 0) + 1
    INTO NEW.registration_number
    FROM public.event_registrations
    WHERE event_id = NEW.event_id;
  END IF;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 6. Trigger on insert
-- Guard: DROP TRIGGER IF EXISTS before CREATE TRIGGER (matches V17 idiom).
DROP TRIGGER IF EXISTS trg_assign_registration_number ON public.event_registrations;
CREATE TRIGGER trg_assign_registration_number
BEFORE INSERT ON public.event_registrations
FOR EACH ROW
EXECUTE FUNCTION public.assign_registration_number();

COMMIT;
