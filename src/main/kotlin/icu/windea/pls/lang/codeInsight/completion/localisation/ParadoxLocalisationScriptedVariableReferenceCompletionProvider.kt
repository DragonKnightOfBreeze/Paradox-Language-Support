package icu.windea.pls.lang.codeInsight.completion.localisation

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.codeInsight.completion.GlobalCompletionContext
import icu.windea.pls.core.processAsync
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionFactory
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionProvider
import icu.windea.pls.lang.codeInsight.completion.ParadoxExtendedCompletionManager
import icu.windea.pls.lang.codeInsight.completion.addToResult
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*

class ParadoxLocalisationScriptedVariableReferenceCompletionProvider : ParadoxCompletionProvider() {
    val elementPattern get() = psiElement().withElementType(SCRIPTED_VARIABLE_REFERENCE_TOKEN)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position

        val globalContext = GlobalCompletionContext.create(element, parameters, context)
        val context = ParadoxCompletionContext.create(globalContext)

        val selector = ParadoxScriptedVariableSearch.selector(context.project, element).contextSensitive().distinct()
        ParadoxScriptedVariableSearch.searchGlobal(null, selector).processAsync { element ->
            ParadoxCompletionFactory.forLocalisationScriptedVariable(element).addToResult(context, result)
        }

        ParadoxExtendedCompletionManager.completeExtendedScriptedVariable(context, result)
    }
}

