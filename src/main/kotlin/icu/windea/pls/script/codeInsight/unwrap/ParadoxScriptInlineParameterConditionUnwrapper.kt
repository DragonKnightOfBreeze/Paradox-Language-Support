package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

class ParadoxScriptInlineParameterConditionUnwrapper : ParadoxScriptUnwrapper() {
    override fun getDescription(e: PsiElement): String {
        val name = if (e is ParadoxScriptInlineParameterCondition) e.presentationText.orUnresolved() else ""
        return PlsBundle.message("script.unwrap.inlineParameterCondition", name)
    }

    override fun isApplicableTo(e: PsiElement): Boolean {
        return e is ParadoxScriptInlineParameterCondition
    }

    override fun doUnwrap(element: PsiElement, context: Context) {
        context.extract(element, element)
        context.delete(element)
    }
}
