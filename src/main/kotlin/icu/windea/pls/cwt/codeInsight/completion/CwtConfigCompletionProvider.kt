package icu.windea.pls.cwt.codeInsight.completion

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
            CwtElementTypes.PROPERTY_KEY_TOKEN -> position.parent?.castOrNull<CwtPropertyKey>()
            CwtElementTypes.STRING_TOKEN -> position.parent?.castOrNull<CwtString>()?.takeIf { it.isPropertyValue() || it.isBlockValue() }
            else -> null
        }
        if (contextElement == null) return

        val r = CwtConfigCompletionManager.initializeContext(parameters, context, contextElement)
        if (!r) return

        CwtConfigCompletionManager.addConfigCompletions(context, result)
    }
}

