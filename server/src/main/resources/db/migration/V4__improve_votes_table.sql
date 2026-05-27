-- V4__improve_votes_table.sql

-- Add unique constraint to prevent duplicate votes (one vote per user per voting)
ALTER TABLE votes ADD CONSTRAINT unique_user_voting UNIQUE (user_id, voting_id);

-- Add timestamps for tracking
ALTER TABLE votes ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE votes ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add index for faster lookups
CREATE INDEX IF NOT EXISTS idx_votes_user_id ON votes(user_id);
CREATE INDEX IF NOT EXISTS idx_votes_voting_id ON votes(voting_id);
