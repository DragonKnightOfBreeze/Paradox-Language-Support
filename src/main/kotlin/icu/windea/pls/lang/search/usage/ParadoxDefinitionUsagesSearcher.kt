package icu.windea.pls.lang.search.usage

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.search.RequestResultProcessor
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.Processor
import icu.windea.pls.core.orNull
import icu.windea.pls.ep.resolve.localisation.ParadoxCompositeLocalisationIconSupport
import icu.windea.pls.ep.resolve.localisation.ParadoxDefinitionBasedLocalisationIconSupport
import icu.windea.pls.ep.resolve.localisation.ParadoxLocalisationIconSupport
import icu.windea.pls.ep.util.data.StellarisGameConceptData
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.getDefinitionData
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionTypeExpression
import icu.windea.pls.lang.wordRequests
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.constraints.ParadoxDefinitionIndexConstraint
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import kotlin.experimental.or

/**
 * 定义的用法的查询。
 *
 * 定义对应的 PSI（[ParadoxDefinitionElement]） 的名字被称为定义的类型键（typeKey），它不一定是定义的名字（definitionName）。
 * 因此，这里需要特殊处理。
 */
class ParadoxDefinitionUsagesSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
    override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
        // TODO SUFFIX_AWARE 不兼容需要带上后缀的情况，目前不支持

        val target = queryParameters.elementToSearch
        if (target !is ParadoxDefinitionElement) return

        val definitionInfo = target.definitionInfo
        if (definitionInfo == null) return
        if (definitionInfo.name.isEmpty()) return // skip anonymous definitions
        val words = getWords(target, definitionInfo)
        val ignoreCase = ParadoxDefinitionIndexConstraint.entries.filter { it.ignoreCase }.any { it.test(definitionInfo.type) }

        // 这里不能直接使用target.useScope，否则文件高亮会出现问题
        val useScope = queryParameters.effectiveSearchScope
        val searchContext = UsageSearchContext.IN_CODE or UsageSearchContext.IN_STRINGS or UsageSearchContext.IN_COMMENTS
        val processor = getProcessor(target)
        queryParameters.optimizer.wordRequests.removeIf { it.word in words }
        for (word in words) {
            queryParameters.optimizer.searchWord(word, useScope, searchContext, !ignoreCase, target, processor)
        }
    }

    private fun getWords(target: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String> {
        val words = mutableSetOf<String>()
        words.add(definitionInfo.name)

        // for <game_concept>
        if (definitionInfo.type == ParadoxDefinitionTypes.gameConcept) {
            val data = target.getDefinitionData<StellarisGameConceptData>()
            data?.alias?.forEach {
                val name = it.orNull()
                if (name != null) words.add(name)
            }
        }

        // from localisation icons
        val nameGetters = mutableSetOf<(String) -> String?>()
        ParadoxLocalisationIconSupport.EP_NAME.extensionList.forEach { support ->
            addToNameGetters(support, definitionInfo, nameGetters)
        }
        nameGetters.forEach { nameGetter ->
            val name = nameGetter(definitionInfo.name)?.orNull()
            if (name != null) words.add(name)
        }

        return words
    }

    private fun addToNameGetters(support: ParadoxLocalisationIconSupport, definitionInfo: ParadoxDefinitionInfo, nameGetters: MutableSet<(String) -> String?>) {
        when (support) {
            is ParadoxCompositeLocalisationIconSupport -> {
                support.supports.forEach { s -> addToNameGetters(s, definitionInfo, nameGetters) }
            }
            is ParadoxDefinitionBasedLocalisationIconSupport -> {
                if (ParadoxDefinitionTypeExpression.resolve(support.definitionType).matches(definitionInfo)) {
                    nameGetters.add(support.nameGetter)
                }
            }
        }
    }

    private fun getProcessor(target: PsiElement): RequestResultProcessor {
        return ParadoxFilteredRequestResultProcessor(target, ParadoxResolveConstraint.Definition)
    }
}

