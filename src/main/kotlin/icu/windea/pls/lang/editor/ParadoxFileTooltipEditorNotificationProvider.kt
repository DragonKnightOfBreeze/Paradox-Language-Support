package icu.windea.pls.lang.editor

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.children
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.core.vfs.VirtualFileService
import icu.windea.pls.lang.codeInsight.generation.ParadoxLocalisationGenerationService
import icu.windea.pls.lang.util.ParadoxLocalisationListManager
import icu.windea.pls.localisation.ParadoxLocalisationFileType
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList
import java.util.function.Function
import javax.swing.JComponent

/**
 * 为带有文件提示元数据的特殊文件提供通知，以便提供额外的说明和快速操作。
 *
 * @see ParadoxLocalisationGenerationService.fileTooltipKey
 */
class ParadoxFileTooltipEditorNotificationProvider : EditorNotificationProvider {
    override fun collectNotificationData(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
        forLocalisationGeneration(project, file)?.let { return it }
        return null
    }

    private fun forLocalisationGeneration(project: Project, file: VirtualFile): Function<in FileEditor, out JComponent?>? {
        if (!VirtualFileService.isLightFile(file)) return null
        if (file.fileType != ParadoxLocalisationFileType) return null
        val fileTooltip = file.getUserData(ParadoxLocalisationGenerationService.fileTooltipKey) ?: return null
        return Function f@{ fileEditor ->
            if (fileEditor !is TextEditor) return@f null
            val panel = EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Info).text(fileTooltip)
            panel.createActionLabel(PlsBundle.message("editor.notification.fileTooltip.action.1")) { copyWithLocale(file, project) }
            panel.createActionLabel(PlsBundle.message("editor.notification.fileTooltip.action.2")) { copyWithoutLocale(file, project) }
            panel
        }
    }

    private fun copyWithLocale(file: VirtualFile, project: Project) {
        val localisationList = getLocalisationList(file, project) ?: return
        ParadoxLocalisationListManager.copyWithLocale(localisationList)
    }

    private fun copyWithoutLocale(file: VirtualFile, project: Project) {
        val localisationList = getLocalisationList(file, project) ?: return
        ParadoxLocalisationListManager.copyWithoutLocale(localisationList)
    }

    private fun getLocalisationList(file: VirtualFile, project: Project): ParadoxLocalisationPropertyList? {
        val psiFile = file.toPsiFile(project) ?: return null
        if (psiFile !is ParadoxLocalisationFile) return null
        val fileLocale = file.getUserData(ParadoxLocalisationGenerationService.fileLocaleKey)
        return psiFile.children().filterIsInstance<ParadoxLocalisationPropertyList>()
            .find { fileLocale == null || it.locale?.name == fileLocale.id }
    }
}
