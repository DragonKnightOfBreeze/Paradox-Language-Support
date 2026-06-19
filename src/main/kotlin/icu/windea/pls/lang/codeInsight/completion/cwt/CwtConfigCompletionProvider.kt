package icu.windea.pls.lang.codeInsight.completion.cwt

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.codeInsight.completion.GlobalCompletionContext
import icu.windea.pls.cwt.psi.CwtElementTypes
import icu.windea.pls.cwt.psi.CwtOptionKey
import icu.windea.pls.cwt.psi.CwtPropertyKey
import icu.windea.pls.cwt.psi.CwtString
import icu.windea.pls.cwt.psi.CwtTokenSets.KEY_OR_STRING_TOKENS
import icu.windea.pls.lang.codeInsight.completion.CwtConfigCompletionContext
import icu.windea.pls.lang.codeInsight.completion.CwtConfigCompletionManager
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionProvider

object CwtConfigCompletionProvider : ParadoxCompletionProvider() {
    val elementPattern get() = psiElement().withElementType(KEY_OR_STRING_TOKENS)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val position = parameters.position
        val contextElement: PsiElement? = when (position.elementType) {
            CwtElementTypes.OPTION_KEY_TOKEN -> position.parent?.castOrNull<CwtOptionKey>()
            CwtElementTypes.PROPERTY_KEY_TOKEN -> position.parent?.castOrNull<CwtPropertyKey>()
            CwtElementTypes.STRING_TOKEN -> position.parent?.castOrNull<CwtString>()
            else -> null
        }
        if (contextElement == null) return

        val globalContext = GlobalCompletionContext.create(contextElement, parameters, context)
        val context = CwtConfigCompletionContext.create(globalContext)
        if (context == null) return

        CwtConfigCompletionManager.addConfigCompletions(context, result)
    }
}
