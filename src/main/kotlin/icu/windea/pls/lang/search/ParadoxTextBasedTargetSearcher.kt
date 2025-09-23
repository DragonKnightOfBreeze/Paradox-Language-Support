package icu.windea.pls.lang.search

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.SearchScope
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import com.intellij.util.Processor
import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.lang.util.PlsCoreManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.codeInsight.ParadoxTargetInfo

/**
 * 基于本地化文本片段的目标（封装变量/定义/本地化）查询器的基类。
 *
 * 流程：输入的文本片段 → 用于查询的文本片段 → 所属的本地 → 相关的封装变量和定义
 */
abstract class ParadoxTextBasedTargetSearcher : QueryExecutorBase<ParadoxTargetInfo, ParadoxTextBasedTargetSearch.SearchParameters>() {
    override fun processQuery(
        queryParameters: ParadoxTextBasedTargetSearch.SearchParameters,
        consumer: Processor<in ParadoxTargetInfo>
    ) {
        // 检查是否启用
        if (!PlsFacade.getSettings().navigation.seForTextBasedTarget) return

        // #141 如果正在为 ParadoxMergedIndex 编制索引并且正在解析引用，则直接跳过
        if (PlsCoreManager.resolveForMergedIndex.get() == true) return

        ProgressManager.checkCanceled()
        if (queryParameters.project.isDefault) return
        if (SearchScope.isEmptyScope(queryParameters.scope)) return
        if (SearchScope.isEmptyScope(queryParameters.restrictedScope)) return

        val context = Context(queryParameters)
        process(context, consumer)
    }

    protected abstract fun process(context: Context, consumer: Processor<in ParadoxTargetInfo>)

    protected abstract fun processText(text: String, context: Context, consumer: Processor<in ParadoxTargetInfo>): Boolean

    protected fun processLeafElement(element: PsiElement, context: Context, consumer: Processor<in ParadoxTargetInfo>): Boolean {
        if (element.elementType != ParadoxLocalisationElementTypes.STRING_TOKEN) return true
        val localisation = element.parentOfType<ParadoxLocalisationProperty>() ?: return true
        return processLocalisation(localisation, context, consumer)
    }

    protected fun processLocalisation(element: ParadoxLocalisationProperty, context: Context, consumer: Processor<in ParadoxTargetInfo>): Boolean {
        val localisation = element
        // 按照以下顺序收集查询结果：封装变量、定义、本地化
        val localisationInfo = ParadoxTargetInfo.from(localisation)
        if (localisationInfo == null) return true
        if (context.processed.add(localisationInfo)) { // 即使目标类型中不包括本地化，也要先去重
            if (context.includeScriptedVariables) {
                ProgressManager.checkCanceled()
                val scriptedVariables = ParadoxLocalisationManager.getRelatedScriptedVariables(localisation)
                for (scriptedVariable in scriptedVariables) {
                    ProgressManager.checkCanceled()
                    val info = ParadoxTargetInfo.from(scriptedVariable) ?: continue
                    if (!context.processed.add(info)) continue
                    if (!consumer.process(info)) return false
                }
            }
            if (context.includeDefinitions) {
                ProgressManager.checkCanceled()
                val definitions = ParadoxLocalisationManager.getRelatedDefinitions(localisation)
                for (definition in definitions) {
                    ProgressManager.checkCanceled()
                    val info = ParadoxTargetInfo.from(definition) ?: continue
                    if (!context.processed.add(info)) continue
                    if (!consumer.process(info)) return false
                }
            }

            if (context.includeLocalisations) {
                ProgressManager.checkCanceled()
                if (!consumer.process(localisationInfo)) return false
            }
        }
        return true
    }

    class Context(
        val queryParameters: ParadoxTextBasedTargetSearch.SearchParameters,
    ) {
        val processed = mutableSetOf<ParadoxTargetInfo>()

        val types = queryParameters.types
        val settings = PlsFacade.getSettings().navigation
        val includeScriptedVariables = types == null || ParadoxSearchTargetType.ScriptedVariable in types
        val includeDefinitions = types == null || ParadoxSearchTargetType.Definition in types
        val includeLocalisations = types == null || ParadoxSearchTargetType.Localisation in types

        val searchHelper = PsiSearchHelper.getInstance(queryParameters.project)
    }
}
