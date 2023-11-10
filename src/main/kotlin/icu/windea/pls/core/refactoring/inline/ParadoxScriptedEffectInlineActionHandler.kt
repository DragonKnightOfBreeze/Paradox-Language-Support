package icu.windea.pls.core.refactoring.inline

import com.intellij.lang.*
import com.intellij.lang.refactoring.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

class ParadoxScriptedEffectInlineActionHandler: InlineActionHandler() {
    override fun getActionName(element: PsiElement?) = PlsBundle.message("title.inline.scriptedEffect")
    
    override fun isEnabledForLanguage(language: Language) = language == ParadoxScriptLanguage
    
    override fun canInlineElement(element: PsiElement): Boolean {
        return element.castOrNull<ParadoxScriptProperty>()?.definitionInfo?.type == "scripted_effect"
    }
    
    override fun inlineElement(project: Project?, editor: Editor?, element: PsiElement) {
        //TODO
    }
}
