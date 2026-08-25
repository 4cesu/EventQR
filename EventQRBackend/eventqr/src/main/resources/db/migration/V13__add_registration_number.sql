-- V13: Per-event attendee registration number (SDD Module 3.7 scope-tracked decision).
-- registration_number is a 1..N sequence PER event_id (not global), assigned by trigger on insert.

-- 1. Add nullable column first
ALTER TABLE public.event_registrations
ADD COLUMN registration_number integer;

-- 2. Backfill existing rows, ordered by created_at (id as tiebreaker)
WITH numbered AS (
  SELECT id,
         row_number() OVER (PARTITION BY event_id ORDER BY created_at, id) AS rn
  FROM public.event_registrations
)
UPDATE public.event_registrations er
SET registration_number = numbered.rn
FROM numbered
WHERE er.id = numbered.id;

-- 3. Enforce NOT NULL now that backfill is done
ALTER TABLE public.event_registrations
ALTER COLUMN registration_number SET NOT NULL;

-- 4. Uniqueness per event
ALTER TABLE public.event_registrations
ADD CONSTRAINT uq_event_registrations_event_regnum
UNIQUE (event_id, registration_number);

-- 5. Trigger function: assign next number on insert (max()+1 per event_id, gaps OK).
-- Concurrency note: MAX()+1 can theoretically race under simultaneous inserts to the same
-- event; the UNIQUE constraint rejects a colliding second insert with a DB error rather than
-- silently duplicating a number. Acceptable trade-off at this scale.
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
CREATE TRIGGER trg_assign_registration_number
BEFORE INSERT ON public.event_registrations
FOR EACH ROW
EXECUTE FUNCTION public.assign_registration_number();
