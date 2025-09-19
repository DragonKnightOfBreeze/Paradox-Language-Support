package icu.windea.pls.ep.tools

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.ep.tools.model.ParadoxModInfo
import java.nio.file.Path
import javax.swing.Icon

/**
 * 模组导出器（平台无关核心接口）。
 *
 * 简介：
 * - 将内存中的模组信息导出为外部格式（如 Paradox Launcher JSON v2/v3，或启动器 SQLite 数据库）。
 * - 顶层接口不直接暴露具体导出方式（JSON/SQLite），通过内部子接口区分。
 *
 * 子接口：
 * - [JsonBased]：导出为 JSON 文本；提供 [toJson] 与基于文件路径的默认实现 [exportTo]。
 * - [SqliteBased]：导出到 SQLite 数据库；提供 [exportToDatabase] 与默认数据库路径 [defaultDbPath]。
 *
 * 约定：
 * - 平台交互/UI 相关信息（如图标/显示文本）由顶层接口统一提供。
 * - 对于 JSON 导出，默认过滤本地来源（Local）的条目，具体行为由各实现决定（见 v2/v3 导出器）。
 * - 对于 SQLite 导出，出于插件体积与兼容性考虑，建议只在类路径存在对应依赖时启用（见实现的 isAvailable）。
 */
interface ParadoxModExporter {
    /** （可选）用于 UI 展示的图标。 */
    val icon: Icon? get() = null
    /** 用于 UI 展示的文本。 */
    val text: String

    /**
     * 是否可用。
     *
     * 用于在运行环境缺失依赖（如 sqlite-jdbc/ktorm）时禁用相应实现。
     */
    fun isAvailable(): Boolean

    /**
     * 基于 JSON 的导出器子接口。
     */
    interface JsonBased : ParadoxModExporter {
        /**
         * 将给定模组信息序列化为目标格式的 JSON 字符串。
         * @param gameId 游戏类型 ID（如 `stellaris`、`vic3`）
         * @param collectionName 集合名称（通常与导出文件名一致）
         * @param mods 模组信息列表（平台无关）
         */
        fun toJson(gameId: String, collectionName: String, mods: List<ParadoxModInfo>): String

        /**
         * 将给定模组信息导出到指定 JSON 文件。
         * 默认实现会调用 [toJson] 并写入 UTF-8 文本。
         */
        fun exportTo(path: Path, gameId: String, mods: List<ParadoxModInfo>) {
            val collectionName = path.fileName.toString().substringBeforeLast('.')
            val json = toJson(gameId, collectionName, mods)
            java.nio.file.Files.createDirectories(path.parent)
            java.nio.file.Files.newBufferedWriter(path).use { it.write(json) }
        }
    }

    /**
     * 基于 SQLite 的导出器子接口。
     */
    interface SqliteBased : ParadoxModExporter {
        /**
         * 将给定模组信息导出到指定 SQLite 数据库。
         * @param dbPath SQLite 数据库文件路径
         * @param gameId 游戏类型 ID
         * @param collectionName 集合名称
         * @param mods 模组信息列表
         */
        fun exportToDatabase(dbPath: Path, gameId: String, collectionName: String, mods: List<ParadoxModInfo>)

        /**
         * 给出默认数据库路径（通常位于游戏数据目录）。
         * @param gameDataPath 游戏数据目录路径
         * @return 默认数据库路径，若无法推断则为 null
         */
        fun defaultDbPath(gameDataPath: Path): Path? = null
    }

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxModExporter>("icu.windea.pls.modExporter")
    }
}
