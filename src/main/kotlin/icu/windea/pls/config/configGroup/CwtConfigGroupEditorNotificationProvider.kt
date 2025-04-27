package icu.windea.pls.config.configGroup

import com.intellij.ide.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.*
import icu.windea.pls.*
import icu.windea.pls.cwt.*
import icu.windea.pls.ep.configGroup.*
import java.util.function.Function
import javax.swing.*

/**
 * 当用户打开CWT规则文件时，给出提示以及一些参考信息。
 */
class CwtConfigGroupEditorNotificationProvider : EditorNotificationProvider, DumbAware {
    override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
        if (file.fileType !is CwtFileType) return null

        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        val fileProvider = fileProviders.find { it.getContainingConfigGroup(file, project) != null }
        if (fileProvider == null) return null
        val message = fileProvider.getNotificationMessage()

        return Function f@{ fileEditor ->
            if (fileEditor !is TextEditor) return@f null
            val panel = EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Info).text(message)
            panel.createActionLabel(PlsBundle.message("configGroup.config.file.guidance")) {
                val url = "https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/references/cwt/guidance.md"
                //val url = "https://github.com/cwtools/cwtools/wiki/.cwt-config-file-guidance"
                BrowserUtil.browse(url)
            }
            panel.createActionLabel(PlsBundle.message("configGroup.config.file.repositories")) {
                val url = "https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/cwt/README.md#repositories"
                BrowserUtil.browse(url)
            }
            panel
        }
    }
}

