CREATE TABLE task (
    id VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    attempts SMALLINT DEFAULT 0,
    next_attempt TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP,
    last_attempt TIMESTAMP(6),
    data TEXT NOT NULL,
    result TEXT,
    error TEXT,
    CONSTRAINT task_pk PRIMARY KEY (id)
);

CREATE INDEX task_status_idx ON task(status);

-- Table: shedlock
CREATE TABLE shedlock (
    name VARCHAR(128),
    lock_until TIMESTAMP(3),
    locked_at TIMESTAMP(3),
    locked_by VARCHAR(255),
    PRIMARY KEY (name)
);

-- Sequence: kafka_consumer_record_id_seq
CREATE SEQUENCE kafka_consumer_record_id_seq;

-- Table: kafka_consumer_record
CREATE TABLE kafka_consumer_record (
    id BIGINT NOT NULL PRIMARY KEY,
    topic VARCHAR(100) NOT NULL,
    partition INT NOT NULL,
    record_offset BIGINT NOT NULL,
    retries INT DEFAULT 0 NOT NULL,
    last_retry TIMESTAMP,
    key BYTEA,
    value BYTEA,
    headers_json TEXT,
    record_timestamp BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (topic, partition, record_offset)
);

-- Index on topic and partition
CREATE INDEX kafka_consumer_record_topic_partition_idx
    ON kafka_consumer_record (topic, partition);

