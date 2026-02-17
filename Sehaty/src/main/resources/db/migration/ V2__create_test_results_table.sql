
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE test_results (

                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              tenant_id VARCHAR(64) NOT NULL,


                            -- Patient information
                              patient_email VARCHAR(255),
                              patient_phone VARCHAR(20),
                              patient_name VARCHAR(255),
                              patient_national_id BYTEA, -- Encrypted
                              patient_id UUID,


                              -- Lab reference
                              laboratory_id UUID NOT NULL,

                              -- Test information
                              test_type VARCHAR(255) NOT NULL,
                              category VARCHAR(100),
                              sub_category VARCHAR(100),
                              test_date TIMESTAMP NOT NULL CHECK (test_date <= CURRENT_TIMESTAMP),


                            -- Status tracking
                              status VARCHAR(50) DEFAULT 'PENDING'
                                  CHECK (status IN ('PENDING','COMPLETED','FAILED','REVIEW')),
                              uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,


    -- Notification tracking
                              email_sent BOOLEAN DEFAULT FALSE,
                              sms_sent BOOLEAN DEFAULT FALSE,
                              notification_sent_at TIMESTAMP,
                              notification_fail_count INT DEFAULT 0,

    -- test file
                              pdf_url VARCHAR(500) NOT NULL



                                  -- Patient actions
                              viewed_at TIMESTAMP,
                              downloaded_at TIMESTAMP,

    -- Audit
                              created_by UUID,

                              FOREIGN KEY (laboratory_id) REFERENCES laboratories(id) ON DELETE CASCADE,
                              FOREIGN KEY (patient_id) REFERENCES users(id) ON DELETE SET NULL






);

CREATE INDEX idx_test_results_patient_email ON test_results(patient_email);
CREATE INDEX idx_test_results_laboratory ON test_results(laboratory_id);
CREATE INDEX idx_test_results_status ON test_results(status);
CREATE INDEX idx_test_results_patient_id ON test_results(patient_id);
CREATE INDEX idx_test_results_test_date ON test_results(test_date);
CREATE INDEX idx_test_results_test_type ON test_results(test_type);
CREATE INDEX idx_test_results_patient_status ON test_results(patient_id, status);