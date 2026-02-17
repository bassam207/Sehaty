CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE laboratories (

                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                              tenant_id VARCHAR(64) NOT NULL UNIQUE,

                              name VARCHAR(255) NOT NULL CHECK (length(trim(name)) > 0),

                              license_number VARCHAR(100) UNIQUE,

                              address TEXT,

                              phone VARCHAR(20),

                              email VARCHAR(255) UNIQUE NOT NULL,

                              status VARCHAR(50) DEFAULT 'PENDING_VERIFICATION'
                                  CHECK (status IN ('PENDING_VERIFICATION','ACTIVE','SUSPENDED','REJECTED')),

                              subscription_tier VARCHAR(50) DEFAULT 'FREE'
                                  CHECK (subscription_tier IN ('FREE','BASIC','PRO','ENTERPRISE')),

                              subscription_start_date TIMESTAMP,
                              subscription_end_date TIMESTAMP,

                              api_key_hash VARCHAR(255) UNIQUE,

                              logo_url VARCHAR(500),

                              is_deleted BOOLEAN DEFAULT FALSE,

                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                              CHECK (
                                  subscription_end_date IS NULL
                                      OR subscription_end_date > subscription_start_date
                                  )
);

CREATE INDEX idx_laboratory_name ON laboratories(name);
CREATE INDEX idx_laboratory_license_number ON laboratories(license_number);
CREATE INDEX idx_laboratory_email ON laboratories(email);