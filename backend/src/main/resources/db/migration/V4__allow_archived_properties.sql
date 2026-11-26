DO $$
DECLARE
    status_constraint RECORD;
BEGIN
    FOR status_constraint IN
        SELECT conname
        FROM pg_constraint
        WHERE conrelid = to_regclass('public.properties')
          AND contype = 'c'
          AND pg_get_constraintdef(oid) ILIKE '%status%'
    LOOP
        EXECUTE format(
            'ALTER TABLE public.properties DROP CONSTRAINT %I',
            status_constraint.conname
        );
    END LOOP;
END $$;

ALTER TABLE public.properties
    ADD CONSTRAINT properties_status_check
    CHECK (status IN ('AVAILABLE', 'RESERVED', 'SOLD', 'ARCHIVED'));
