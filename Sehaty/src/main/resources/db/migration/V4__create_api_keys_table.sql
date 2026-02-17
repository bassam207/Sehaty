

-- Table to store API keys for laboratories
CREATE TABLE api_keys (
    -- Primary key
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Reference to the laboratory that owns this API key
                          laboratory_id UUID NOT NULL,

    -- Key prefix used for identification (non-secret part)
                          key_prefix VARCHAR(20) NOT NULL,

    -- Hashed API key (store hash, never plaintext)
                          key_hash VARCHAR(255) NOT NULL,

    -- Optional descriptive name for the key
                          name VARCHAR(255),

    -- Status of the API key (active/inactive)
                          is_active BOOLEAN DEFAULT TRUE,

    -- Timestamp of last usage
                          last_used_at TIMESTAMP,

    -- Creation timestamp
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Expiration timestamp for key rotation
                          expires_at TIMESTAMP,

    -- Foreign key constraint to the laboratories table
                          FOREIGN KEY (laboratory_id) REFERENCES laboratories(id) ON DELETE CASCADE
);

-- Indexes to improve search performance
CREATE INDEX idx_api_keys_prefix ON api_keys(key_prefix); -- Search by key prefix
CREATE INDEX idx_api_keys_laboratory ON api_keys(laboratory_id); -- Search all keys for a lab
CREATE INDEX idx_api_keys_active ON api_keys(is_active); -- Filter active keys quickly


