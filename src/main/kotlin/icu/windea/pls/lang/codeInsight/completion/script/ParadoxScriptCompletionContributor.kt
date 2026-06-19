package icu.windea.pls.lang.codeInsight.completion.script

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionInitializationContext
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import icu.windea.pls.model.constants.PlsConstants

class ParadoxScriptCompletionContributor : CompletionContributor() {
    init {
        ParadoxKeywordCompletionProvider.let { extend(CompletionType.BASIC, it.elementPattern, it) }
        ParadoxScriptedVariableNameCompletionProvider.let { extend(CompletionType.BASIC, it.elementPattern, it) }
        ParadoxDefinitionNameCompletionProvider.let { extend(CompletionType.BASIC, it.elementPattern, it) }
        ParadoxDefineNameCompletionProvider.let { extend(CompletionType.BASIC, it.elementPattern, it) }
        ParadoxVariableNameCompletionProvider.let { extend(CompletionType.BASIC, it.elementPattern, it) }
        ParadoxScriptedVariableReferenceCompletionProvider.let { extend(null, it.elementPattern, it) }
        ParadoxScriptExpressionCompletionProvider.let { extend(null, it.elementPattern, it) }
        ParadoxEventIdCompletionProvider.let { extend(null, it.elementPattern, it) }
        ParadoxParameterCompletionProvider.let { extend(null, it.elementPattern, it) }
        ParadoxInlineScriptUsageCompletionProvider.let { extend(null, it.elementPattern, it) }
        ParadoxDefinitionInjectionExpressionCompletionProvider.let { extend(null, it.elementPattern, it) }
    }

    override fun beforeCompletion(context: CompletionInitializationContext) {
        context.dummyIdentifier = PlsConstants.dummyIdentifier
    }

    @Suppress("RedundantOverride")
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        super.fillCompletionVariants(parameters, result)
    }
}
