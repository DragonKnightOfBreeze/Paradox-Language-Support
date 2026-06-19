package icu.windea.pls.lang.codeInsight.completion.localisation

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.util.ProcessingContext
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.icon
import icu.windea.pls.core.processAsync
import icu.windea.pls.ep.util.data.StellarisGameConceptData
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionProvider
import icu.windea.pls.lang.codeInsight.completion.addElement
import icu.windea.pls.lang.codeInsight.completion.withCompletionId
import icu.windea.pls.lang.getDefinitionData
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.localisation.psi.ParadoxLocalisationConceptName
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.psi.isDatabaseObjectExpression
import icu.windea.pls.model.constants.ParadoxDefinitionTypes

object ParadoxLocalisationConceptCompletionProvider : ParadoxCompletionProvider() {
    val elementPattern get() = psiElement(CONCEPT_NAME_TOKEN)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parent?.castOrNull<ParadoxLocalisationConceptName>() ?: return
        if (element.text.isParameterized(conditionBlock = false)) return
        if (element.isDatabaseObjectExpression(strict = true)) return

        val file = parameters.originalFile
        val project = file.project

        ParadoxCompletionManager.initializeContext(parameters, context)

        // 提示concept的name或alias
        val conceptSelector = ParadoxDefinitionSearch.selector(project, file).contextSensitive().distinct()
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
