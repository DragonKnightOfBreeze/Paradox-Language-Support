package icu.windea.pls.ep.tools.exporter

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.fileChooser.FileSaverDescriptor
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.tools.ParadoxModSetInfo
import java.nio.file.Path
import javax.swing.Icon

/**
 * 模组导出器。
 *
 * （目前）用于在游戏或模组设置的对话框中，从模组依赖列表导出模组信息到各种数据文件。
 *
 * @property icon 用于 UI 展示的图标。
 * @property text 用于 UI 展示的文本。
 *
 * @see icu.windea.pls.lang.ui.tools.ParadoxModDependenciesExportPopup
 */
interface ParadoxModExporter {
    val icon: Icon? get() = null
    val text: String

    /**
     * 检查是否可用。
     */
    fun isAvailable(gameType: ParadoxGameType): Boolean

    /**
     * 执行导出操作，将模组集信息（[modSetInfo]）导出到指定路径（[filePath]）的数据文件。
     */
    suspend fun execute(filePath: Path, modSetInfo: ParadoxModSetInfo): Result

    /**
     * 创建 [FileSaverDescriptor] ，用于弹出保存文件的对话框，从而获取数据文件的目标路径。
     */
    fun createFileSaverDescriptor(gameType: ParadoxGameType): FileSaverDescriptor

    /** 得到默认要保存到的目录。*/
    fun getSavedBaseDir(gameType: ParadoxGameType): Path?

    /** 得到默认要保存到的文件名。*/
    fun getSavedFileName(gameType: ParadoxGameType): String?

    /**
     * 导出结果。
     *
     * @property total 原始的条目数。
     * @property actualTotal 最终导入的条目数。
     * @property warning 警告信息。
     */
    data class Result(
        val total: Int,
        val actualTotal: Int,
        val warning: String? = null
    )

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxModExporter>("icu.windea.pls.modExporter")

        fun getAll(gameType: ParadoxGameType): List<ParadoxModExporter> {
            return EP_NAME.extensionList.filter { it.isAvailable(gameType) }
        }
    }
}
