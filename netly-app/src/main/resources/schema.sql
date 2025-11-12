-- PostgreSQL Database Setup Script for Netly with Authentication

-- Create database (run this separately as postgres user)
-- CREATE DATABASE netly_app;

-- Connect to the database
\c netly_app;

---- Create User ----
CREATE USER netly_app_user WITH ENCRYPTED PASSWORD '123456';

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
    currency VARCHAR(10) DEFAULT 'INR',
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
    currency VARCHAR(10) DEFAULT 'INR',
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

-- Create currency_rates table
CREATE TABLE IF NOT EXISTS netly_schema.currency_rates (
   id BIGSERIAL PRIMARY KEY,
   user_id BIGINT NOT NULL,
   currency_code VARCHAR(10) NOT NULL,
    currency_name VARCHAR(255) NOT NULL,
    rate_to_inr NUMERIC(15,6) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES netly_schema.users(id) ON DELETE CASCADE,
    UNIQUE(user_id, currency_code)
    );

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_currency_rates_user_id ON netly_schema.currency_rates(user_id);
CREATE INDEX IF NOT EXISTS idx_currency_rates_currency_code ON netly_schema.currency_rates(currency_code);

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
