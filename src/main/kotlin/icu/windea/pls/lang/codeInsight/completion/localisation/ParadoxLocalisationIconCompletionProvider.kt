package icu.windea.pls.lang.codeInsight.completion.localisation

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionProvider
import icu.windea.pls.lang.codeInsight.completion.contextElement
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.resolve.ParadoxLocalisationIconService
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationIcon

object ParadoxLocalisationIconCompletionProvider : ParadoxCompletionProvider() {
    val elementPattern get() = psiElement(ICON_TOKEN)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parent?.castOrNull<ParadoxLocalisationIcon>() ?: return
        if (element.text.isParameterized(conditionBlock = false)) return

        ParadoxCompletionManager.initializeContext(parameters, context)
        context.contextElement = element

        ParadoxLocalisationIconService.complete(context, result)
    }
}
