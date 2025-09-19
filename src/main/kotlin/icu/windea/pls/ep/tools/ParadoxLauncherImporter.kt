package icu.windea.pls.ep.tools

// Ktorm imports are compile-only; keep them at top to satisfy style.
import icu.windea.pls.PlsBundle
import icu.windea.pls.ep.tools.model.ParadoxModImportData
import icu.windea.pls.ep.tools.model.ParadoxModInfo
import icu.windea.pls.model.ParadoxModSource
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.asc
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.innerJoin
import org.ktorm.dsl.limit
import org.ktorm.dsl.map
import org.ktorm.dsl.orderBy
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists

// 表定义（最小字段集，兼容 v2/v4/v5 结构）
private object Playsets : Table<Nothing>("Playsets") {
    val id = int("Id").primaryKey()
    val name = varchar("Name")
    val isActive = int("IsActive")
}
private object PlaysetsMods : Table<Nothing>("PlaysetsMods") {
    val playsetId = int("PlaysetId")
    val modId = int("ModId")
    val position = int("Position")
    val enabled = int("Enabled")
}
private object Mods : Table<Nothing>("Mods") {
    val id = int("Id").primaryKey()
    val gameRegistryId = varchar("GameRegistryId")
    val dirPath = varchar("DirPath")
    val displayName = varchar("DisplayName")
}

/**
 * 从 Paradox 启动器的 SQLite 数据库导入模组配置（平台无关数据模型）。
 *
 * 简介：
 * - 连接启动器数据库（如 `launcher-v2.sqlite`），查询“激活的集合（playset）”与启用的模组清单，
 *   映射为通用的 [ParadoxModImportData]（包含集合名与模组列表）。
 * - 为避免插件包体积增加与运行环境差异，本实现仅在类路径同时存在 `sqlite-jdbc` 与 `ktorm` 时可用（见 [isAvailable]）。
 *
 * 版本兼容：
 * - 以最小字段集（`Playsets/PlaysetsMods/Mods`）进行查询，可兼容 v2/v4/v5 常见表结构。
 * - 字段：`Playsets(Id, Name, IsActive)`，`PlaysetsMods(PlaysetId, ModId, Position, Enabled)`，`Mods(Id, GameRegistryId, DirPath, DisplayName)`。
 *
 * 注意：
 * - 无法从数据库直接区分 Steam/Paradox 的远端来源，故此处将 [ParadoxModInfo.source] 统一映射为 Local，
 *   远端 ID（`steamId/pdxId`）若存在则保存在 [ParadoxModInfo.remoteId]，调用端可按需二次推断。
 *
 * 参考链接：
 * - Irony Mod Manager 对应实现：`ParadoxLauncherImporter.cs`
 *   [https://github.com/bcssov/IronyModManager/blob/master/src/IronyModManager.IO/Mods/Importers/ParadoxLauncherImporter.cs]
 */
open class ParadoxLauncherImporter : ParadoxModImporter.SqliteBased {
    companion object {
        private const val dbPath = "launcher-v2.sqlite"
    }

    override val text: String = PlsBundle.message("mod.importer.launcher")

    open fun getDbPath(gameDataPath: Path): Path = gameDataPath.resolve(dbPath)

    override fun isAvailable(): Boolean = try {
        Class.forName("org.sqlite.JDBC")
        Class.forName("org.ktorm.database.Database")
        true
    } catch (_: Throwable) {
        false
    }

    override fun defaultDbPath(gameDataPath: Path): Path? = getDbPath(gameDataPath)

    override fun importFromDatabase(dbPath: Path): ParadoxModImportData {
        // 防御：若不可用或文件不存在，抛出异常以便 UI 层提示
        if (!isAvailable()) error("SQLite/Ktorm is not available in classpath.")
        if (!dbPath.exists()) error("Database file not found: $dbPath")

        val url = "jdbc:sqlite:${dbPath.toAbsolutePath()}"
        val db = Database.connect(url)

        // 取激活的集合
        val activePlayset = db
            .from(Playsets)
            .select(Playsets.id, Playsets.name)
            .where { Playsets.isActive eq 1 }
            .limit(1)
            .map { row -> row[Playsets.id]!! to row[Playsets.name].orEmpty() }
            .firstOrNull() ?: (0 to "")
        val (playsetId, playsetName) = activePlayset

        // 关联查询启用的模组，按 position 升序
        val mods = db
            .from(PlaysetsMods)
            .innerJoin(Mods, on = PlaysetsMods.modId eq Mods.id)
            .select(Mods.displayName, Mods.dirPath, Mods.gameRegistryId, PlaysetsMods.position, PlaysetsMods.enabled)
            .where { (PlaysetsMods.playsetId eq playsetId) and (PlaysetsMods.enabled eq 1) }
            .orderBy(PlaysetsMods.position.asc())
            .map { row ->
                ParadoxModInfo(
                    name = row[Mods.displayName],
                    modDirectory = row[Mods.dirPath]?.let { Paths.get(it) },
                    remoteId = row[Mods.gameRegistryId],
                    source = ParadoxModSource.Local, // 无法区分 steam/pdx，仅作占位
                    enabled = (row[PlaysetsMods.enabled] ?: 1) == 1,
                )
            }

        return ParadoxModImportData(
            gameId = null,
            collectionName = playsetName.ifBlank { null },
            mods = mods,
        )
    }
}

