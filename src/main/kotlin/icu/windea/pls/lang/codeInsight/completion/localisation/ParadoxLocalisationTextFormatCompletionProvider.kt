package icu.windea.pls.lang.codeInsight.completion.localisation

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.progress.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*

@WithGameType(ParadoxGameType.Ck3, ParadoxGameType.Vic3)
class ParadoxLocalisationTextFormatCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parent?.castOrNull<ParadoxLocalisationTextFormat>() ?: return
        if (element.text.isParameterized(conditionBlock = false)) return

        val definitionType = ParadoxDefinitionTypes.TextFormat
        val icon = PlsIcons.Nodes.LocalisationTextFormat //使用特定图标
        val tailText = " from <$definitionType>"
        val originalFile = parameters.originalFile
        val project = originalFile.project
        val definitionSelector = selector(project, originalFile).definition().contextSensitive().distinctByName()
        ParadoxDefinitionSearch.search(definitionType, definitionSelector).processQueryAsync p@{ definition ->
            ProgressManager.checkCanceled()
            val definitionInfo = definition.definitionInfo ?: return@p true
            val name = definitionInfo.name
            if (name.isEmpty()) return@p true

            val typeFile = definition.containingFile
            val lookupElement = LookupElementBuilder.create(definition, name).withIcon(icon)
                .withTailText(tailText, true)
                .withTypeText(typeFile.name, typeFile.icon, true)
                .withCompletionId()
                .withCaseSensitivity(false)
            result.addElement(lookupElement, context)
            true
        }
    }
}
