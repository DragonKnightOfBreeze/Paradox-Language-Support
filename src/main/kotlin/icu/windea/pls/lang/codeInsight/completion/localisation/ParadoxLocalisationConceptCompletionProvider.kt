package icu.windea.pls.lang.codeInsight.completion.localisation

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.ProcessingContext
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.codeInsight.completion.GlobalCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionProvider
import icu.windea.pls.lang.codeInsight.completion.ParadoxLocalisationCompletionManager
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptName
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.psi.isDatabaseObjectExpression

/**
 * 提供概念的名字和别名的代码补全。
 */
class ParadoxLocalisationConceptCompletionProvider : ParadoxCompletionProvider() {
    val elementPattern get() = psiElement(CONCEPT_NAME_TOKEN)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parent?.castOrNull<ParadoxLocalisationConceptName>() ?: return
        if (element.text.isParameterized(conditionBlock = false)) return
        if (element.isDatabaseObjectExpression(strict = true)) return

        val globalContext = GlobalCompletionContext.create(element, parameters, context)
        val context = ParadoxCompletionContext.create(globalContext)

        ParadoxLocalisationCompletionManager.completeConcept(context, result)
    }
}
