

-- Table to log important actions performed by laboratories
CREATE TABLE laboratory_activity_logs (
    -- Primary key
                                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Reference to the laboratory performing the action
                                          laboratory_id UUID NOT NULL,

    -- Timestamp of the action
                                          action_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Action type (e.g., UPLOAD_TEST_RESULT, SEND_NOTIFICATION, UPDATE_PROFILE, GENERATE_REPORT)
                                          action_type VARCHAR(100) NOT NULL,

    -- Optional count of items affected (e.g., number of test results uploaded)
                                          count INTEGER DEFAULT 1 CHECK (count >= 0),

    -- Additional metadata for extra details (e.g., affected patient_id, IP, device)
                                          metadata JSONB,

    -- Foreign key to laboratories table
                                          FOREIGN KEY (laboratory_id) REFERENCES laboratories(id) ON DELETE CASCADE
);

-- Indexes for fast search/filter
CREATE INDEX idx_lab_activity_laboratory ON laboratory_activity_logs(laboratory_id);
CREATE INDEX idx_lab_activity_action_type ON laboratory_activity_logs(action_type);
CREATE INDEX idx_lab_activity_time ON laboratory_activity_logs(action_at);

-- Composite index for common queries: find all actions by lab + type + time range
CREATE INDEX idx_lab_activity_laboratory_type_time
    ON laboratory_activity_logs(laboratory_id, action_type, action_at);

-- Optional: CHECK constraint for allowed action types
ALTER TABLE laboratory_activity_logs
    ADD CONSTRAINT chk_lab_activity_type
        CHECK (action_type IN (
                               'UPLOAD_TEST_RESULT',
                               'SEND_NOTIFICATION',
                               'UPDATE_PROFILE',
                               'GENERATE_REPORT',
                               'DELETE_TEST_RESULT',
                               'OTHER'
            ));
