-- Minimal V4+ schema for launcher database used in tests
PRAGMA foreign_keys=OFF;
BEGIN TRANSACTION;

-- knex_migrations records include the V4 migration name to mark INTEGER position
CREATE TABLE IF NOT EXISTS knex_migrations (
  id INTEGER PRIMARY KEY,
  name TEXT NOT NULL,
  batch INTEGER,
  migration_time TEXT
);

-- playsets table (strings as IDs in newer schemas)
CREATE TABLE IF NOT EXISTS playsets (
  id TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  isActive INTEGER NOT NULL DEFAULT 0,
  loadOrder TEXT,
  createdOn datetime NOT NULL,
  updatedOn datetime,
  syncedOn datetime
);

-- mods table
CREATE TABLE IF NOT EXISTS mods (
  id TEXT PRIMARY KEY,
  pdxId TEXT,
  steamId TEXT,
  gameRegistryId TEXT,
  name TEXT,
  displayName TEXT,
  source TEXT
);

-- playsets_mods table (position INTEGER in real V4, but we store as text and keep numeric string for test)
CREATE TABLE IF NOT EXISTS playsets_mods (
  playsetId TEXT NOT NULL,
  modId TEXT NOT NULL,
  enabled INTEGER NOT NULL DEFAULT 1,
  position TEXT,
  PRIMARY KEY (playsetId, modId)
);

COMMIT;
