-- Minimal V2 schema for launcher database used in tests (position stored as TEXT)
PRAGMA foreign_keys=OFF;
BEGIN TRANSACTION;

-- knex_migrations without V4 marker
CREATE TABLE IF NOT EXISTS knex_migrations (
  id INTEGER PRIMARY KEY,
  name TEXT NOT NULL,
  batch INTEGER,
  migration_time TEXT
);
INSERT INTO knex_migrations(name, batch, migration_time) VALUES ('initial', 1, '2020-01-01 00:00:00');

-- playsets table
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
INSERT INTO mods(id, steamId, displayName, source) VALUES ('mod_rs4',  '937289339', 'Real Space 4.0', 'steam');

-- playsets_mods table (position TEXT with left-padded decimals)
CREATE TABLE IF NOT EXISTS playsets_mods (
  playsetId TEXT NOT NULL,
  modId TEXT NOT NULL,
  enabled INTEGER NOT NULL DEFAULT 1,
  position TEXT,
  PRIMARY KEY (playsetId, modId)
);
INSERT INTO playsets_mods(playsetId, modId, enabled, position) VALUES ('ps1', 'mod_uiod', 1, '0000004097');
INSERT INTO playsets_mods(playsetId, modId, enabled, position) VALUES ('ps1', 'mod_pd',   1, '0000004098');
INSERT INTO playsets_mods(playsetId, modId, enabled, position) VALUES ('ps1', 'mod_rs4',  1, '0000004099');

COMMIT;
