package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.icon
import icu.windea.pls.core.processAsync
import icu.windea.pls.ep.util.data.StellarisGameConceptData
import icu.windea.pls.lang.getDefinitionData
import icu.windea.pls.lang.resolve.ParadoxLocalisationIconService
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.model.constants.ParadoxDefinitionTypes

object ParadoxLocalisationCompletionManager {
    fun completeIcon(context: ParadoxCompletionContext, result: CompletionResultSet) {
        ParadoxLocalisationIconService.complete(context, result)
    }

    fun completeConcept(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val conceptSelector = ParadoxDefinitionSearch.selector(context.project, context.file).contextSensitive().distinct()
        val keysToDistinct = mutableSetOf<String>()
        ParadoxDefinitionSearch.searchProperty(null, ParadoxDefinitionTypes.gameConcept, conceptSelector).processAsync p@{ concept ->
            val tailText = " from concepts"
            val typeFile = concept.containingFile
            val icon = PlsIcons.Nodes.LocalisationConceptCommand
            run action@{
                val key = concept.name
                if (key.isEmpty()) return@action
                if (!keysToDistinct.add(key)) return@action
                val lookupElement = LookupElementBuilder.create(concept, key)
                    .withIcon(icon)
                    .withTailText(tailText, true)
                    .withTypeText(typeFile?.name, typeFile?.icon, true)
                    .withCompletionId()
                result.addElement(lookupElement, context)
            }
            concept.getDefinitionData<StellarisGameConceptData>()?.alias?.forEach action@{ alias ->
                val key = alias
                if (key.isEmpty()) return@action
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
