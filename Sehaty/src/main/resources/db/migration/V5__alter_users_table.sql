-- File: V5__alter_users_table.sql

-- Alter users table to add additional columns for patient/laboratory management
ALTER TABLE users
    ADD COLUMN phone_number VARCHAR(20), -- User phone number
ADD COLUMN national_id BYTEA, -- Encrypted national ID (use pgcrypto)
ADD COLUMN role VARCHAR(50) DEFAULT 'PATIENT' -- User role
    CHECK (role IN ('PATIENT', 'LAB_ADMIN', 'SUPER_ADMIN')),
ADD COLUMN laboratory_id UUID, -- Reference to laboratory if user belongs to a lab
ADD COLUMN email_verified BOOLEAN DEFAULT FALSE, -- Email verification status
ADD COLUMN phone_verified BOOLEAN DEFAULT FALSE, -- Phone verification status
ADD COLUMN email_notifications_enabled BOOLEAN DEFAULT TRUE, -- User preference for email notifications
ADD COLUMN sms_notifications_enabled BOOLEAN DEFAULT TRUE, -- User preference for SMS notifications
ADD COLUMN verification_token VARCHAR(255), -- Token for email/phone verification
ADD COLUMN verification_token_expires_at TIMESTAMP; -- Expiry of the verification token

-- Foreign key to associate user with a laboratory
ALTER TABLE users
    ADD CONSTRAINT fk_user_laboratory
        FOREIGN KEY (laboratory_id) REFERENCES laboratories(id) ON DELETE SET NULL;

-- Indexes to improve search performance
CREATE INDEX idx_users_national_id ON users(national_id); -- Search by national ID
CREATE INDEX idx_users_phone_number ON users(phone_number); -- Search by phone number
CREATE INDEX idx_users_role ON users(role); -- Filter users by role
CREATE INDEX idx_users_laboratory_id ON users(laboratory_id); -- Search users belonging to a specific laboratory
CREATE INDEX idx_users_email_verified ON users(email_verified); -- Filter verified users quickly
