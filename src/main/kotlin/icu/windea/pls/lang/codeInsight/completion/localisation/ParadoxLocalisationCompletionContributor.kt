package icu.windea.pls.lang.codeInsight.completion.localisation

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import icu.windea.pls.model.constants.PlsConstants

class ParadoxLocalisationCompletionContributor : CompletionContributor() {
    init {
        ParadoxLocalisationLocaleCompletionProvider().let { extend(CompletionType.BASIC, it.elementPattern, it) }
        ParadoxLocalisationNameCompletionProvider().let { extend(CompletionType.BASIC, it.elementPattern, it) }
        ParadoxLocalisationColorCompletionProvider().let { extend(null, it.elementPattern, it) }
        ParadoxLocalisationParameterCompletionProvider().let { extend(null, it.elementPattern, it) }
        ParadoxScriptedVariableReferenceCompletionProvider().let { extend(null, it.elementPattern, it) }
        ParadoxLocalisationExpressionCompletionProvider().let { extend(null, it.elementPattern, it) }
        ParadoxLocalisationIconCompletionProvider().let { extend(null, it.elementPattern, it) }
        ParadoxLocalisationConceptCompletionProvider().let { extend(null, it.elementPattern, it) }
        ParadoxLocalisationTextFormatCompletionProvider().let { extend(null, it.elementPattern, it) }
        ParadoxLocalisationTextIconCompletionProvider().let { extend(null, it.elementPattern, it) }
    }

    override fun beforeCompletion(context: CompletionInitializationContext) {
        context.dummyIdentifier = PlsConstants.dummyIdentifier
    }

    @Suppress("RedundantOverride")
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        super.fillCompletionVariants(parameters, result)
    }
}
