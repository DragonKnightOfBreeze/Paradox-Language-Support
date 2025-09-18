package icu.windea.pls.ep.tools

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.ep.tools.model.ParadoxModImportData
import java.nio.file.Path
import javax.swing.Icon

/**
 * 模组导入器（平台无关核心接口）。
 *
 * 主要入口方法不依赖 IntelliJ 平台，可在单元测试或其他环境直接调用。
 * 用于从外部格式（如 Paradox Launcher JSON v3、dlc_load.json）读取模组信息。
 *
 * @property icon （可选）用于 UI 展示的图标。
 * @property text 用于 UI 展示的文本。
 */
interface ParadoxModImporter {
    val icon: Icon? get() = null
    val text: String

    /**
     * 是否可用。
     */
    fun isAvailable(): Boolean

    /**
     * 从指定 JSON 文件导入模组信息。
     * @param jsonPath JSON 文件路径
     * @return 导入结果（包含集合名/游戏ID/模组列表）
     */
    fun importFromJson(jsonPath: Path): ParadoxModImportData

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxModImporter>("icu.windea.pls.modImporter")
    }
}
