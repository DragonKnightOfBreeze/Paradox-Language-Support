package icu.windea.pls.lang.codeInsight.completion.cwt

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.castOrNull
import icu.windea.pls.cwt.psi.CwtElementTypes
import icu.windea.pls.cwt.psi.CwtOptionKey
import icu.windea.pls.cwt.psi.CwtPropertyKey
import icu.windea.pls.cwt.psi.CwtString
import icu.windea.pls.lang.codeInsight.completion.CwtConfigCompletionManager

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
