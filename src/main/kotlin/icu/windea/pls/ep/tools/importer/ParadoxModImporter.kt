package icu.windea.pls.ep.tools.importer

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.tools.ParadoxModSetInfo
import java.nio.file.Path
import javax.swing.Icon

/**
 * 模组导入器。
 *
 * （目前）用于在游戏或模组设置的对话框中，从各种数据文件导入模组信息到模组依赖列表中。
 *
 * @property icon 用于 UI 展示的图标。
 * @property text 用于 UI 展示的文本。
 *
 * @see icu.windea.pls.lang.ui.tools.ParadoxModDependenciesImportPopup
 */
interface ParadoxModImporter {
    val icon: Icon? get() = null
    val text: String

    /**
     * 检查是否可用。
     */
    fun isAvailable(gameType: ParadoxGameType): Boolean

    /**
     * 执行导入操作，从指定路径（[filePath]）的数据文件导入数据到模组集信息（[modSetInfo]）。
     */
    suspend fun execute(filePath: Path, modSetInfo: ParadoxModSetInfo): Result

    /** 创建 [FileChooserDescriptor] ，用于弹出选择文件的对话框，从而获取数据文件的目标路径。*/
    fun createFileChooserDescriptor(gameType: ParadoxGameType): FileChooserDescriptor

    /** 得到默认选择的文件。*/
    fun getSelectedFile(gameType: ParadoxGameType): Path?

    /**
     * 导入结果。
     *
     * @property total 原始的条目数。
     * @property actualTotal 最终导出的条目数。
     * @property newModSetInfo 需要导入的新模组集信息。
     * @property warning 警告信息。
     */
    data class Result(
        val total: Int,
        val actualTotal: Int,
        val newModSetInfo: ParadoxModSetInfo,
        val warning: String? = null
    )

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxModImporter>("icu.windea.pls.modImporter")

        fun getAll(gameType: ParadoxGameType): List<ParadoxModImporter> {
            return EP_NAME.extensionList.filter { it.isAvailable(gameType) }
        }
    }
}
