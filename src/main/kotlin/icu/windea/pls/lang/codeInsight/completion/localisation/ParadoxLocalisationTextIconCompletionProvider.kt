package icu.windea.pls.lang.codeInsight.completion.localisation

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.util.ProcessingContext
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.icon
import icu.windea.pls.core.processAsync
import icu.windea.pls.lang.annotations.WithGameType
import icu.windea.pls.lang.codeInsight.completion.addElement
import icu.windea.pls.lang.codeInsight.completion.withCompletionId
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.definitionName
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.distinctBy
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.localisation.psi.ParadoxLocalisationTextIcon
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes

@WithGameType(ParadoxGameType.Ck3, ParadoxGameType.Vic3, ParadoxGameType.Eu5)
class ParadoxLocalisationTextIconCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        val element = parameters.position.parent?.castOrNull<ParadoxLocalisationTextIcon>() ?: return
        if (element.text.isParameterized(conditionBlock = false)) return

        val definitionType = ParadoxDefinitionTypes.textIcon
        val icon = PlsIcons.Nodes.LocalisationTextIcon // 使用特定图标
        val tailText = " from <$definitionType>"
        val originalFile = parameters.originalFile
        val project = originalFile.project
        val definitionSelector = selector(project, originalFile).definition().contextSensitive()
            .distinctBy { it.definitionName }
        ParadoxDefinitionSearch.search(null, definitionType, definitionSelector).processAsync p@{ definition ->
            ProgressManager.checkCanceled()
            val definitionInfo = definition.definitionInfo ?: return@p true
            val name = definitionInfo.name
            if (name.isEmpty()) return@p true

            val typeFile = definition.containingFile
            val lookupElement = LookupElementBuilder.create(definition, name).withIcon(icon)
                .withTailText(tailText, true)
                .withTypeText(typeFile.name, typeFile.icon, true)
                .withCompletionId()
            result.addElement(lookupElement, context)
            true
        }
    }
}
