package icu.windea.pls.ep.tools.model

import org.ktorm.entity.Entity

/**
 * 官方启动器数据库表：schema_migrations。
 *
 * 用于标识数据库 schema 版本，例如是否包含 V4（position 字段由 TEXT 变为 INTEGER）。
 * 参见：IronyModManager 的相关导入/导出实现（Schema migrations 检测）。
 */
interface SchemaMigrationEntity : Entity<SchemaMigrationEntity> {
    companion object : Entity.Factory<SchemaMigrationEntity>()

    var id: Int
    var name: String
    // var batch: Int
    // var migrationTime: LocalDateTime
}

/**
 * 官方启动器数据库表：playsets（播放集）。
 *
 * 关键字段：
 * - id：主键
 * - name：播放集名称
 * - isActive：是否为当前激活的播放集
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
 * 官方启动器数据库表：mods（模组）。
 *
 * 关键字段：
 * - id：主键
 * - displayName：显示名称
 * - steamId / pdxId：远端 ID（优先使用 steamId）
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
 * 官方启动器数据库表：playset_mods（播放集-模组 映射）。
 *
 * 关键字段：
 * - playsetId：播放集 ID
 * - modId：模组 ID
 * - position：在播放集中的顺序
 *   - V2：TEXT（十进制字符串，常见为左侧补零）
 *   - V4+：INTEGER（0 起步）
 */
interface PlaysetsModEntity : Entity<PlaysetsModEntity> {
    companion object : Entity.Factory<PlaysetsModEntity>()

    var playsetId: String
    var modId: String
    val enabled: Boolean
    var position: String?
}

