package icu.windea.pls.ep.tools

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.ep.tools.model.ParadoxModImportData
import java.nio.file.Path
import javax.swing.Icon

/**
 * 模组导入器（平台无关核心接口）。
 *
 * 主要入口方法不依赖 IntelliJ 平台，可在单元测试或其他环境直接调用。
 * 用于从外部来源（如 Paradox Launcher JSON、启动器 SQLite 数据库、dlc_load.json 等）读取模组信息。
 *
 * - 顶层接口不直接暴露具体载入方式（JSON/SQLite），以内部子接口进行区分：
 *   - [JsonBased]：基于 JSON 文件的导入。
 *   - [SqliteBased]：基于 SQLite 数据库的导入。
 * - 平台交互/UI 相关信息（如图标/显示文本）由顶层接口统一提供。
 */
interface ParadoxModImporter {
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
     * 基于 JSON 文件的导入器子接口。
     */
    interface JsonBased : ParadoxModImporter {
        /**
         * 从指定 JSON 文件导入模组信息。
         * @param jsonPath JSON 文件路径
         * @return 导入结果（包含集合名/游戏ID/模组列表）
         */
        fun importFromJson(jsonPath: Path): ParadoxModImportData
    }

    /**
     * 基于 SQLite 数据库的导入器子接口。
     */
    interface SqliteBased : ParadoxModImporter {
        /**
         * 从指定 SQLite 数据库导入模组信息。
         * @param dbPath SQLite 数据库文件路径
         */
        fun importFromDatabase(dbPath: Path): ParadoxModImportData

        /**
         * 给出默认数据库路径（通常位于游戏数据目录）。
         * @param gameDataPath 游戏数据目录路径
         * @return 默认数据库路径，若无法推断则为 null
         */
        fun defaultDbPath(gameDataPath: Path): Path? = null
    }

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxModImporter>("icu.windea.pls.modImporter")
    }
}
