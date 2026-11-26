ALTER TABLE orders DROP CONSTRAINT check_status;

ALTER TABLE orders ADD CONSTRAINT check_status
    CHECK (status IN ('CREATED', 'PAID', 'PENDING', 'CANCELLED'));