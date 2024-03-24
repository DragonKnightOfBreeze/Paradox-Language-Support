package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.*
import com.intellij.codeInsight.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.actions.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

/**
 * 导航到当前定义/修正的相关本地化的动作。
 */
class GotoRelatedLocalisationsAction : BaseCodeInsightAction() {
    private val handler = GotoRelatedLocalisationsHandler()
    
    override fun getHandler(): CodeInsightActionHandler {
        return handler
    }
    
    override fun isValidForFile(project: Project, editor: Editor, file: PsiFile): Boolean {
        return file is ParadoxScriptFile && file.fileInfo != null
    }
    
    override fun update(event: AnActionEvent) {
        val presentation = event.presentation
        presentation.isEnabledAndVisible = false
        val project = event.project
        val editor = event.editor
        if(editor == null || project == null) return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project)
        if(file !is ParadoxScriptFile) return
        presentation.isVisible = true
        if(file.definitionInfo != null) {
            presentation.isEnabled = true
            return
        }
        val offset = editor.caretModel.offset
        val element = findElement(file, offset)
        val isEnabled = when {
            element == null -> false
            element.isDefinitionRootKeyOrName() -> true
            ParadoxModifierHandler.resolveModifier(element) != null -> true
            else -> false
        }
        presentation.isEnabled = isEnabled
    }
    
    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptStringExpressionElement? {
        return ParadoxPsiManager.findScriptExpression(file, offset).castOrNull()
    }
}

