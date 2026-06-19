package icu.windea.pls.lang.codeInsight.completion

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.progress.ProgressManager
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.codeInsight.LimitedCompletionProcessor
import icu.windea.pls.core.icon
import icu.windea.pls.core.processAsync
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.ep.util.data.StellarisGameConceptData
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.getDefinitionData
import icu.windea.pls.lang.resolve.ParadoxLocalisationIconService
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.filterBy
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes

object ParadoxLocalisationCompletionManager {
    fun completeLocalisationName(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val file = context.file as? ParadoxLocalisationFile ?: return
        val type = ParadoxLocalisationType.resolve(file) ?: return

        // 本地化的提示结果可能有上千条，因此这里改为先按照输入的关键字过滤结果，关键字变更时重新提示
        result.restartCompletionOnAnyPrefixChange()

        // 提示 `localisation` 或者 `synced_localisation`
        val selector = ParadoxLocalisationSearch.selector(context.project, file)
            .contextSensitive()
            .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
            .filterBy { it.name != context.keyword } // skip if name = input
        val processor = LimitedCompletionProcessor<ParadoxLocalisationProperty> {
            ProgressManager.checkCanceled()
            val name = it.name
            val icon = it.icon
            val typeFile = it.containingFile
            val lookupElement = LookupElementBuilder.create(it, name)
                .withIcon(icon)
                .withTypeText(typeFile.name, typeFile.icon, true)
            result.addElement(lookupElement)
            true
        }
        // 保证索引在此 readAction 中可用
        runSmartReadAction(context.project, inSmartMode = true) {
            ParadoxLocalisationSearch.processVariants(type, result.prefixMatcher, selector, processor)
        }
    }

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

    fun completeTextFormat(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val definitionType = ParadoxDefinitionTypes.textFormat
        val icon = PlsIcons.Nodes.LocalisationTextFormat // 使用特定图标
        val tailText = " from <$definitionType>"
        val definitionSelector = ParadoxDefinitionSearch.selector(context.project, context.file).contextSensitive().distinct()
        ParadoxDefinitionSearch.searchProperty(null, definitionType, definitionSelector).processAsync p@{ definition ->
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

    fun completeTextIcon(context: ParadoxCompletionContext, result: CompletionResultSet) {
        val definitionType = ParadoxDefinitionTypes.textIcon
        val icon = PlsIcons.Nodes.LocalisationTextIcon // 使用特定图标
        val tailText = " from <$definitionType>"
        val definitionSelector = ParadoxDefinitionSearch.selector(context.project, context.file).contextSensitive().distinct()
        ParadoxDefinitionSearch.searchProperty(null, definitionType, definitionSelector).processAsync p@{ definition ->
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
