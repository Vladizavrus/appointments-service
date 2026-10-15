CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE appointments ADD CONSTRAINT no_overlap
    EXCLUDE USING gist (specialist WITH =, tsrange(start_at, end_at) WITH &&)
    WHERE (status <> 'CANCELLED');