DO $$
BEGIN
    IF to_regclass('public.users') IS NOT NULL THEN
        ALTER TABLE public.users DROP CONSTRAINT IF EXISTS users_role_check;
        UPDATE public.users SET role = 'SUPER_ADMIN' WHERE role = 'ADMIN';
        ALTER TABLE public.users
            ADD CONSTRAINT users_role_check
            CHECK (role IN ('SUPER_ADMIN', 'OWNER', 'AGENT', 'CUSTOMER'));
    END IF;
END $$;

ALTER TABLE public.properties ADD COLUMN IF NOT EXISTS base_price NUMERIC(14,2);
