package icu.windea.pls.core.refactoring.inline

import com.intellij.codeInsight.*
import com.intellij.lang.*
import com.intellij.lang.refactoring.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.refactoring.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

class ParadoxInlineScriptInlineActionHandler : InlineActionHandler() {
    override fun getActionName(element: PsiElement?) = PlsBundle.message("title.inline.inlineScript")
    
    override fun isEnabledForLanguage(language: Language) = language == ParadoxScriptLanguage
    
    override fun canInlineElement(element: PsiElement): Boolean {
        if(element !is ParadoxScriptFile) return false
        if(ParadoxInlineScriptHandler.getInlineScriptExpression(element) == null) return false
        return false
    }
    
    override fun canInlineElementInEditor(element: PsiElement, editor: Editor?): Boolean {
        val reference = if(editor != null) TargetElementUtil.findReference(editor, editor.caretModel.offset) else null
        if(reference != null && ParadoxInlineScriptHandler.getInlineScriptExpressionFromExpression(reference.element) == null) return false
        return super.canInlineElementInEditor(element, editor)
    }
    
    override fun inlineElement(project: Project, editor: Editor?, element: PsiElement) {
        return performInline(project, editor, element.cast())
    }
    
    private fun performInline(project: Project, editor: Editor?, element: ParadoxScriptFile) {
        val reference = if(editor != null) TargetElementUtil.findReference(editor, editor.caretModel.offset) else null
        
        val configContext = CwtConfigHandler.getConfigContext(element) ?: return //unexpected
        if(configContext.inlineScriptHasRecursion != true) {
            val message = PlsBundle.message("refactoring.inlineScript.recursive", getRefactoringName())
            CommonRefactoringUtil.showErrorHint(project, editor, message, getRefactoringName(), null)
            return
        }
        
        val dialog = ParadoxInlineScriptInlineDialog(project, element, reference, editor)
        dialog.show()
    }
    
    private fun getRefactoringName() = PlsBundle.message("title.inline.inlineScript")
}
