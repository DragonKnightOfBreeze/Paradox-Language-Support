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
INSERT INTO knex_migrations(name, batch, migration_time) VALUES ('modifyPositionToInteger', 1, '2024-01-01 00:00:00');

-- playsets table (strings as IDs in newer schemas)
CREATE TABLE IF NOT EXISTS playsets (
  id TEXT PRIMARY KEY,
  name TEXT NOT NULL,
  isActive INTEGER NOT NULL DEFAULT 0,
  loadOrder TEXT
);
INSERT INTO playsets(id, name, isActive) VALUES ('ps1', 'Test Playset', 1);

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
INSERT INTO mods(id, steamId, displayName, source) VALUES ('mod_uiod', '1623423360', 'UI Overhaul Dynamic', 'steam');
INSERT INTO mods(id, steamId, displayName, source) VALUES ('mod_pd',   '819148835',  'Planetary Diversity', 'steam');
INSERT INTO mods(id, steamId, displayName, source) VALUES ('mod_rs4',  '1260739267', 'Real Space 4.0', 'steam');

-- playsets_mods table (position INTEGER in real V4, but we store as text and keep numeric string for test)
CREATE TABLE IF NOT EXISTS playsets_mods (
  playsetId TEXT NOT NULL,
  modId TEXT NOT NULL,
  enabled INTEGER NOT NULL DEFAULT 1,
  position TEXT,
  PRIMARY KEY (playsetId, modId)
);
INSERT INTO playsets_mods(playsetId, modId, enabled, position) VALUES ('ps1', 'mod_uiod', 1, '0');
INSERT INTO playsets_mods(playsetId, modId, enabled, position) VALUES ('ps1', 'mod_pd',   1, '1');
INSERT INTO playsets_mods(playsetId, modId, enabled, position) VALUES ('ps1', 'mod_rs4',  1, '2');

COMMIT;
