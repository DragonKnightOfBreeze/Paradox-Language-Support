package icu.windea.pls.lang.codeInsight.completion.localisation

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.icon
import icu.windea.pls.core.processQueryAsync
import icu.windea.pls.ep.data.StellarisGameConceptData
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.addElement
import icu.windea.pls.lang.codeInsight.completion.withCompletionId
import icu.windea.pls.lang.getData
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.distinctByName
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptName
import icu.windea.pls.localisation.psi.isDatabaseObjectExpression
import icu.windea.pls.model.constants.ParadoxDefinitionTypes

/**
 * 提供概念的名字和别名的代码补全。
 */
class ParadoxLocalisationConceptCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parent?.castOrNull<ParadoxLocalisationConceptName>() ?: return
        if (element.text.isParameterized(conditionBlock = false)) return
        if (element.isDatabaseObjectExpression(strict = true)) return

        val file = parameters.originalFile
        val project = file.project

        ParadoxCompletionManager.initializeContext(parameters, context)

        //提示concept的name或alias
        val conceptSelector = selector(project, file).definition().contextSensitive().distinctByName()
        val keysToDistinct = mutableSetOf<String>()
        ParadoxDefinitionSearch.search(null, ParadoxDefinitionTypes.GameConcept, conceptSelector).processQueryAsync p@{ concept ->
            val tailText = " from concepts"
            val typeFile = concept.containingFile
            val icon = PlsIcons.Nodes.LocalisationConcept
            run action@{
                val key = concept.name
                if (!keysToDistinct.add(key)) return@action
                val lookupElement = LookupElementBuilder.create(concept, key)
                    .withIcon(icon)
                    .withTailText(tailText, true)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withCompletionId()
                result.addElement(lookupElement, context)
            }
            concept.getData<StellarisGameConceptData>()?.alias?.forEach action@{ alias ->
                val key = alias
                if (!keysToDistinct.add(key)) return@action
                val lookupElement = LookupElementBuilder.create(concept, key)
                    .withIcon(icon)
                    .withTailText(tailText, true)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withCompletionId()
                result.addElement(lookupElement, context)
            }
            true
        }
    }
}
