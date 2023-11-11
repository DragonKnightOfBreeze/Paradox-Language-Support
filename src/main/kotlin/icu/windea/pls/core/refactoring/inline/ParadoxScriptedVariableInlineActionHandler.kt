package icu.windea.pls.core.refactoring.inline

import com.intellij.lang.*
import com.intellij.lang.refactoring.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

class ParadoxScriptedVariableInlineActionHandler: InlineActionHandler() {
    override fun getActionName(element: PsiElement?) = PlsBundle.message("title.inline.scriptedVariable")
    
    override fun isEnabledForLanguage(language: Language) = language.isParadoxLanguage()
    
    override fun canInlineElement(element: PsiElement): Boolean {
        return element is ParadoxScriptScriptedVariable
    }
    
    override fun inlineElement(project: Project?, editor: Editor?, element: PsiElement) {
        
    }
}

