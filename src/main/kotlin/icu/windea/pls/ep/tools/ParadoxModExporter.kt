package icu.windea.pls.ep.tools

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.ep.tools.model.ParadoxModInfo
import java.nio.file.Path
import javax.swing.Icon

/**
 * 模组导出器（平台无关核心接口）。
 *
 * 主要入口方法不依赖 IntelliJ 平台，可在单元测试或其他环境直接调用。
 * 用于将内存中的模组信息导出为外部格式（如 Paradox Launcher JSON v2/v3）。
 *
 * @property icon （可选）用于 UI 展示的图标。
 * @property text 用于 UI 展示的文本。
 */
interface ParadoxModExporter {
    val icon: Icon? get() = null
    val text: String

    /**
     * 是否可用。
     */
    fun isAvailable(): Boolean

    /**
     * 将给定模组信息序列化为目标格式的 JSON 字符串。
     * @param gameId 游戏类型 ID（如 `stellaris`、`vic3`）
     * @param collectionName 集合名称（通常与导出文件名一致）
     * @param mods 模组信息列表（平台无关）
     */
    fun toJson(gameId: String, collectionName: String, mods: List<ParadoxModInfo>): String

    /**
     * 将给定模组信息导出到指定文件。
     * 默认实现会调用 [toJson] 并写入 UTF-8 文本。
     */
    fun exportTo(path: Path, gameId: String, mods: List<ParadoxModInfo>) {
        val collectionName = path.fileName.toString().substringBeforeLast('.')
        val json = toJson(gameId, collectionName, mods)
        java.nio.file.Files.createDirectories(path.parent)
        java.nio.file.Files.newBufferedWriter(path).use { it.write(json) }
    }

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxModExporter>("icu.windea.pls.modExporter")
    }
}
