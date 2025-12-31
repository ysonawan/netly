-- PostgreSQL Database Setup Script for Netly with Authentication

-- Create database (run this separately as postgres user)
-- CREATE DATABASE netly_app;

-- Connect to the database
\c netly_app;

---- Create User ----
-- https://vault.zoho.in#/unlock/extension?routeName=%23%2Fpasscard%2F63500000000007049
CREATE USER netly_app_user WITH ENCRYPTED PASSWORD '<Replace with password from zoho vault>';

---- Grant privileges to the user ----
GRANT CONNECT ON DATABASE netly_app TO netly_app_user;

---- Create Schema and Set copilot_user as Owner ----
CREATE SCHEMA IF NOT EXISTS netly_schema AUTHORIZATION netly_app_user;

---- Grant usage on schema ----
GRANT USAGE, CREATE ON SCHEMA netly_schema TO netly_app_user;

-- Set search path to use the schema
SET search_path TO netly_schema;

-- Create users table with password
CREATE TABLE IF NOT EXISTS netly_schema.users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    secondary_emails TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create assets table with user relationship
CREATE TABLE IF NOT EXISTS netly_schema.assets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    custom_asset_type_id BIGINT,
    current_value NUMERIC(15,2) NOT NULL,
    purchase_price NUMERIC(15,2),
    purchase_date DATE,
    quantity NUMERIC(10,2),
    description TEXT,
    location VARCHAR(255),
    illiquid BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES netly_schema.users(id) ON DELETE CASCADE
);

-- Create liabilities table with user relationship
CREATE TABLE IF NOT EXISTS netly_schema.liabilities (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    custom_liability_type_id BIGINT,
    current_balance NUMERIC(15,2) NOT NULL,
    original_amount NUMERIC(15,2),
    start_date DATE,
    end_date DATE,
    interest_rate NUMERIC(5,2),
    monthly_payment NUMERIC(15,2),
    lender VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES netly_schema.users(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_assets_user_id ON netly_schema.assets(user_id);
CREATE INDEX IF NOT EXISTS idx_assets_custom_asset_type_id ON netly_schema.assets(custom_asset_type_id);
CREATE INDEX IF NOT EXISTS idx_assets_created_at ON netly_schema.assets(created_at);
CREATE INDEX IF NOT EXISTS idx_liabilities_user_id ON netly_schema.liabilities(user_id);
CREATE INDEX IF NOT EXISTS idx_liabilities_custom_liability_type_id ON netly_schema.liabilities(custom_liability_type_id);
CREATE INDEX IF NOT EXISTS idx_liabilities_created_at ON netly_schema.liabilities(created_at);
CREATE INDEX IF NOT EXISTS idx_users_email ON netly_schema.users(email);

-- Migration to add custom asset and liability types tables

CREATE TABLE IF NOT EXISTS netly_schema.custom_asset_types (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type_name VARCHAR(100) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES netly_schema.users(id) ON DELETE CASCADE,
    UNIQUE (user_id, type_name)
);

CREATE INDEX idx_custom_asset_types_user_id ON netly_schema.custom_asset_types(user_id);

CREATE TABLE IF NOT EXISTS netly_schema.custom_liability_types (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type_name VARCHAR(100) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES netly_schema.users(id) ON DELETE CASCADE,
    UNIQUE (user_id, type_name)
);

CREATE INDEX idx_custom_liability_types_user_id ON netly_schema.custom_liability_types(user_id);

-- Add foreign key constraints for assets and liabilities to reference custom types
ALTER TABLE netly_schema.assets
    ADD CONSTRAINT fk_assets_custom_asset_type
    FOREIGN KEY (custom_asset_type_id) REFERENCES netly_schema.custom_asset_types(id);

ALTER TABLE netly_schema.liabilities
    ADD CONSTRAINT fk_liabilities_custom_liability_type
    FOREIGN KEY (custom_liability_type_id) REFERENCES netly_schema.custom_liability_types(id);

-- Create portfolio snapshots table for weekly tracking
CREATE TABLE IF NOT EXISTS netly_schema.portfolio_snapshots (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    snapshot_date DATE NOT NULL,
    total_assets NUMERIC(15,2) NOT NULL,
    total_liabilities NUMERIC(15,2) NOT NULL,
    net_worth NUMERIC(15,2) NOT NULL,
    total_gains NUMERIC(15,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES netly_schema.users(id) ON DELETE CASCADE,
    UNIQUE (user_id, snapshot_date)
);

CREATE INDEX IF NOT EXISTS idx_portfolio_snapshots_user_id ON netly_schema.portfolio_snapshots(user_id);
CREATE INDEX IF NOT EXISTS idx_portfolio_snapshots_snapshot_date ON netly_schema.portfolio_snapshots(snapshot_date);
CREATE INDEX IF NOT EXISTS idx_portfolio_snapshots_user_date ON netly_schema.portfolio_snapshots(user_id, snapshot_date);

-- Create asset snapshots table
CREATE TABLE IF NOT EXISTS netly_schema.asset_snapshots (
    id BIGSERIAL PRIMARY KEY,
    portfolio_snapshot_id BIGINT NOT NULL,
    asset_id BIGINT,
    asset_name VARCHAR(255) NOT NULL,
    asset_type_name VARCHAR(100) NOT NULL,
    current_value NUMERIC(15,2) NOT NULL,
    gain_loss NUMERIC(15,2),
    currency VARCHAR(10),
    value_in_inr NUMERIC(15,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (portfolio_snapshot_id) REFERENCES netly_schema.portfolio_snapshots(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_asset_snapshots_portfolio_snapshot_id ON netly_schema.asset_snapshots(portfolio_snapshot_id);
CREATE INDEX IF NOT EXISTS idx_asset_snapshots_asset_id ON netly_schema.asset_snapshots(asset_id);

-- Create liability snapshots table
CREATE TABLE IF NOT EXISTS netly_schema.liability_snapshots (
    id BIGSERIAL PRIMARY KEY,
    portfolio_snapshot_id BIGINT NOT NULL,
    liability_id BIGINT,
    liability_name VARCHAR(255) NOT NULL,
    liability_type_name VARCHAR(100) NOT NULL,
    current_balance NUMERIC(15,2) NOT NULL,
    currency VARCHAR(10),
    balance_in_inr NUMERIC(15,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (portfolio_snapshot_id) REFERENCES netly_schema.portfolio_snapshots(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_liability_snapshots_portfolio_snapshot_id ON netly_schema.liability_snapshots(portfolio_snapshot_id);
CREATE INDEX IF NOT EXISTS idx_liability_snapshots_liability_id ON netly_schema.liability_snapshots(liability_id);

-- Create budget_items table for personal budget management
CREATE TABLE IF NOT EXISTS netly_schema.budget_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    item_type VARCHAR(20) NOT NULL, -- INCOME or EXPENSE
    item_name VARCHAR(255) NOT NULL,
    amount NUMERIC(15,2) NOT NULL,
    is_investment BOOLEAN NOT NULL DEFAULT false,
    description TEXT,
    display_order INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES netly_schema.users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_budget_items_user_id ON netly_schema.budget_items(user_id);
CREATE INDEX IF NOT EXISTS idx_budget_items_item_type ON netly_schema.budget_items(item_type);
CREATE INDEX IF NOT EXISTS idx_budget_items_user_type ON netly_schema.budget_items(user_id, item_type);

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA netly_schema TO netly_app_user;

GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA netly_schema TO netly_app_user;

ALTER DEFAULT PRIVILEGES IN SCHEMA netly_schema GRANT SELECT ON SEQUENCES TO netly_app_user;
