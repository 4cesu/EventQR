-- V14: Set default for transaction_logs.metadata and backfill existing NULL/blank values.
--
-- IDEMPOTENCY GUARDS:
--   ALTER COLUMN SET DEFAULT → guarded by information_schema.columns check for the column
--   UPDATE backfill → WHERE clause makes it idempotent (only touches NULL/blank values)
--
-- LEGACY-DRIFT SAFE: On a drifted DB where ddl-auto=update already set this default
-- and V16 already includes DEFAULT '{}', both statements are no-ops. On a greenfield DB
-- the column exists (V16 creates it), so the ALTER is a harmless no-op and the UPDATE
-- finds no rows to change.

DO $$ BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public'
      AND table_name = 'transaction_logs'
      AND column_name = 'metadata'
  ) THEN
    ALTER TABLE public.transaction_logs
    ALTER COLUMN metadata SET DEFAULT '{}';

    UPDATE public.transaction_logs
    SET metadata = '{}'
    WHERE metadata IS NULL
       OR btrim(metadata) = '';
  END IF;
END $$;
