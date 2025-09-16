package icu.windea.pls.config.configGroup

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import icu.windea.pls.PlsBundle
import icu.windea.pls.cwt.CwtFileType
import icu.windea.pls.ep.configGroup.CwtConfigGroupFileProvider
import java.util.function.Function
import javax.swing.JComponent

/**
 * 当用户打开CWT规则文件时，给出提示以及一些参考信息。
 */
class CwtConfigGroupEditorNotificationProvider : EditorNotificationProvider, DumbAware {
    override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
        if (file.fileType !is CwtFileType) return null

        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        val (fileProvider, configGroup) = fileProviders.firstNotNullOfOrNull { fileProvider ->
            fileProvider.getContainingConfigGroup(file, project)?.let { configGroup -> fileProvider to configGroup }
        } ?: return null
        val message = fileProvider.getNotificationMessage(configGroup)
        if (message.isNullOrEmpty()) return null

        return Function f@{ fileEditor ->
            if (fileEditor !is TextEditor) return@f null
            val panel = EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Info).text(message)
            panel.createActionLabel(PlsBundle.message("configGroup.config.file.guidance")) {
                val url = "https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/blob/master/references/cwt/guidance.md"
                // val url = "https://github.com/cwtools/cwtools/wiki/.cwt-config-file-guidance"
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

