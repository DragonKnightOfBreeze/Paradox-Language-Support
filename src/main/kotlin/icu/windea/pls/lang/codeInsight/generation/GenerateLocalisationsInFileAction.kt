package icu.windea.pls.lang.codeInsight.generation

import com.intellij.codeInsight.*
import com.intellij.codeInsight.actions.*
import com.intellij.codeInsight.generation.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.psi.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.actions.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

class GenerateLocalisationsInFileAction : BaseCodeInsightAction(), GenerateActionPopupTemplateInjector {
    private val handler = ParadoxGenerateLocalisationsHandler(forFile = true)

    override fun getHandler(): CodeInsightActionHandler {
        return handler
    }

    override fun update(event: AnActionEvent) {
        val presentation = event.presentation
        presentation.isEnabledAndVisible = false
        val project = event.project ?: return
        val editor = event.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if (file !is ParadoxScriptFile && file !is ParadoxLocalisationFile) return
        if (file.fileInfo == null) return
        presentation.isEnabledAndVisible = true
    }

    override fun createEditTemplateAction(dataContext: DataContext?): AnAction? {
        return null
    }
}
