package icu.windea.pls.lang.codeInsight.generation

import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.codeInsight.generation.actions.GenerateActionPopupTemplateInjector
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.lang.actions.editor
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.script.psi.ParadoxScriptFile

class GenerateLocalisationsAction : BaseCodeInsightAction(), GenerateActionPopupTemplateInjector {
    private val handler = GenerateLocalisationsHandler()

    override fun getHandler() = handler

    override fun update(event: AnActionEvent) {
        val presentation = event.presentation
        presentation.isEnabledAndVisible = false
        val project = event.project ?: return
        val editor = event.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if (file !is ParadoxScriptFile && file !is ParadoxLocalisationFile) return
        if (file.fileInfo == null) return
        presentation.isVisible = true
        handler.updateContext(file, editor)
        if (handler.context == null) return
        presentation.isEnabled = true
    }

    override fun createEditTemplateAction(dataContext: DataContext?): AnAction? {
        return null
    }
}
