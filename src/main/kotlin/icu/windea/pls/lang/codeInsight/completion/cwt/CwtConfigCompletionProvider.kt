package icu.windea.pls.lang.codeInsight.completion.cwt

import com.intellij.codeInsight.completion.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.codeInsight.completion.*

class CwtConfigCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val position = parameters.position
        val contextElement: PsiElement? = when (position.elementType) {
            CwtElementTypes.OPTION_KEY_TOKEN -> position.parent?.castOrNull<CwtOptionKey>()
            CwtElementTypes.PROPERTY_KEY_TOKEN -> position.parent?.castOrNull<CwtPropertyKey>()
            CwtElementTypes.STRING_TOKEN -> position.parent?.castOrNull<CwtString>()
            else -> null
        }
        if (contextElement == null) return

        val r = CwtConfigCompletionManager.initializeContext(contextElement, parameters, context)
        if (!r) return
        val r1 = CwtConfigCompletionManager.initializeContextForConfigCompletions(context)
        if (!r1) return

        CwtConfigCompletionManager.addConfigCompletions(context, result)
    }
}
