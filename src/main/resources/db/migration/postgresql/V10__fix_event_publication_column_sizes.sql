ALTER TABLE event_publication
    ALTER COLUMN serialized_event TYPE TEXT,
    ALTER COLUMN listener_id      TYPE VARCHAR(1024),
    ALTER COLUMN event_type       TYPE VARCHAR(1024);
