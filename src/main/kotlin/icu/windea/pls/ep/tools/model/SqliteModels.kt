package icu.windea.pls.ep.tools.model

import org.ktorm.entity.Entity

/**
 * `knex_migrations` 表的实体类。
 *
 * 用于标识数据库 schema 版本，例如是否包含 V4（position 字段由 TEXT 变为 INTEGER）。
 * 参见：IronyModManager 的相关导入/导出实现（Schema migrations 检测）。
 *
 * @see KnexMigrations
 */
interface KnexMigrationEntity : Entity<KnexMigrationEntity> {
    companion object : Entity.Factory<KnexMigrationEntity>()

    var id: Int
    var name: String
    // var batch: Int
    // var migrationTime: LocalDateTime
}

/**
 * `playsets` 表的实体类。播放集信息。
 *
 * @see Playsets
 */
interface PlaysetEntity : Entity<PlaysetEntity> {
    companion object : Entity.Factory<PlaysetEntity>()

    var id: String
    var name: String
    var isActive: Boolean
    var loadOrder: String?
    // ignore other columns
}

/**
 * `mods` 表的实体类。模组信息。
 *
 * @see Mods
 */
interface ModEntity : Entity<ModEntity> {
    companion object : Entity.Factory<ModEntity>()

    var id: String
    var pdxId: String?
    var steamId: String?
    var gameRegistryId: String?
    var name: String?
    var displayName: String?
    var source: String?
    // ignore other columns
}

/**
 * `playset_mods` 表的实体类。播放集与模组信息的映射。
 *
 * 关键字段：
 * - playsetId：播放集 ID
 * - modId：模组 ID
 * - position：在播放集中的顺序
 *   - V2：TEXT（十进制字符串，常见为左侧补零）
 *   - V4+：INTEGER（0 起步）
 *
 * @see PlaysetsMods
 */
interface PlaysetsModEntity : Entity<PlaysetsModEntity> {
    companion object : Entity.Factory<PlaysetsModEntity>()

    var playsetId: String
    var modId: String
    val enabled: Boolean
    var position: String?
}
