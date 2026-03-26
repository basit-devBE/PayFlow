CREATE TABLE event_publication
(
    id                      UUID         NOT NULL PRIMARY KEY,
    listener_id             VARCHAR(255) NOT NULL,
    event_type              VARCHAR(255) NOT NULL,
    serialized_event        VARCHAR(255) NOT NULL,
    publication_date        TIMESTAMPTZ  NOT NULL,
    completion_date         TIMESTAMPTZ,
    last_resubmission_date  TIMESTAMPTZ,
    completion_attempts     INTEGER      NOT NULL DEFAULT 0,
    status                  VARCHAR(20)  NOT NULL DEFAULT 'PUBLISHED'
);
