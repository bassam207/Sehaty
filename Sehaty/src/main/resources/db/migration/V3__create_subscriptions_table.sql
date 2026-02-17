

-- Table to track laboratory subscriptions
CREATE TABLE subscriptions (
    -- Primary key
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    -- Reference to the laboratory
                               laboratory_id UUID NOT NULL,

    -- Subscription tier (e.g., FREE, BASIC, PRO, ENTERPRISE)
                               tier VARCHAR(50) NOT NULL
                                   CHECK (tier IN ('FREE','BASIC','PRO','ENTERPRISE')),

    -- Current status of the subscription
                               status VARCHAR(50) DEFAULT 'ACTIVE'
                                   CHECK (status IN ('ACTIVE','SUSPENDED','CANCELLED','EXPIRED')),

    -- Subscription start and end dates
                               start_date TIMESTAMP NOT NULL,
                               end_date TIMESTAMP,

    -- Limits and usage
                               max_results_per_month INTEGER CHECK (max_results_per_month >= 0),
                               results_used_this_month INTEGER DEFAULT 0 CHECK (results_used_this_month >= 0),

    -- Monthly subscription price
                               monthly_price DECIMAL(10,2),

    -- Timestamps for auditing
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraint to the laboratories table
                               FOREIGN KEY (laboratory_id) REFERENCES laboratories(id) ON DELETE CASCADE
);

-- Indexes to improve query performance
CREATE INDEX idx_subscriptions_laboratory ON subscriptions(laboratory_id); -- Search by laboratory
CREATE INDEX idx_subscriptions_status ON subscriptions(status); -- Filter by subscription status
CREATE INDEX idx_subscriptions_laboratory_status
    ON subscriptions(laboratory_id, status); -- Composite index for active subscription lookup

-- Trigger function to automatically update `updated_at` on row update
CREATE OR REPLACE FUNCTION set_subscriptions_updated_at()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger that executes before any update on subscriptions
CREATE TRIGGER trg_subscriptions_updated_at
    BEFORE UPDATE ON subscriptions
    FOR EACH ROW
    EXECUTE FUNCTION set_subscriptions_updated_at();
