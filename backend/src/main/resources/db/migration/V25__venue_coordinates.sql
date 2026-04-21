ALTER TABLE venues
  ADD COLUMN IF NOT EXISTS latitude  DECIMAL(9, 6),
  ADD COLUMN IF NOT EXISTS longitude DECIMAL(9, 6);

CREATE INDEX idx_venues_location ON venues(latitude, longitude)
  WHERE latitude IS NOT NULL AND longitude IS NOT NULL;
