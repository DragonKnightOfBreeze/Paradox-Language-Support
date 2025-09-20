@file:Suppress("unused")

package icu.windea.pls.ep.tools.model

import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import org.ktorm.schema.text
import org.ktorm.schema.varchar

/**
 * `knex_migrations` 表。
 */
object KnexMigrations : Table<SchemaMigrationEntity>(tableName = "knex_migrations") {
    val id = int("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    // val batch = int("batch").bindTo { it.batch }
    // val migrationTime = datetime("migration_time").bindTo { it.migrationTime }
}

/**
 * `playsets` 表。
 */
object Playsets : Table<PlaysetEntity>(tableName = "playsets") {
    val id = varchar("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val isActive = boolean("isActive").bindTo { it.isActive }
    val loadOrder = varchar("loadOrder").bindTo { it.loadOrder }
    // ignore other columns
}

/**
 * `mods` 表。
 */
object Mods : Table<ModEntity>(tableName = "mods") {
    val id = varchar("id").primaryKey().bindTo { it.id }
    val pdxId = varchar("pdxId").bindTo { it.pdxId }
    val steamId = varchar("steamId").bindTo { it.steamId } // e.g., 1623423360
    val gameRegistryId = text("gameRegistryId").bindTo { it.gameRegistryId } // e.g., mod/ugc_1623423360.mod
    val name = varchar("name").bindTo { it.name }
    val displayName = varchar("displayName").bindTo { it.displayName } // e.g., UI Overhaul Dynamic
    var source = varchar("source").bindTo { it.source } // local, steam, etc.
    // ignore other columns
}

/**
 * `playsets_mods` 表。
 *
 * 注意：position 在 V2 是 TEXT（字符串），在 V4+ 是 INTEGER。这里统一映射为字符串，
 * 由业务层负责选择写入格式（左侧补零的字符串或整数值的字符串）。
 */
object PlaysetsMods : Table<PlaysetsModEntity>(tableName = "playsets_mods") {
    val playsetId = varchar("playsetId").bindTo { it.playsetId }
    val modId = varchar("modId").bindTo { it.modId }
    val enabled = boolean("enabled").bindTo { it.enabled } // default to true
    val position = varchar("position").bindTo { it.position }
}
