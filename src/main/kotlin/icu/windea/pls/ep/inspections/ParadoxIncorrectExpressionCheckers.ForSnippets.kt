package icu.windea.pls.ep.inspections

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.expandConfigExpression
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.collections.mapNotNullFast
import icu.windea.pls.core.joinToStringFast
import icu.windea.pls.core.util.ProcessorScope
import icu.windea.pls.ep.ChronicleEpBundle
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionContext
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.util.ParadoxLocaleManager

/**
 * 检查定义引用片段（[CwtDataTypes.DefinitionSnippet]）是否完全匹配。
 *
 * 说明：
 * - 语义匹配阶段采用宽松策略，仅要求至少一个模板参数能够解析为存在的定义。
 * - 此检查器要求所有模板参数都能解析为存在的定义，否则报告“部分匹配”。
 *
 * @see CwtDataTypes.DefinitionSnippet
 */
class ParadoxDefinitionSnippetChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, context: ParadoxExpressionInspectionContext): Boolean {
        val configExpression = ProcessorScope.findFrom<CwtDataExpression>({ config.expandConfigExpression { process(it) } }) { it.type == CwtDataTypes.DefinitionSnippet } ?: return true
        val typeExpression = configExpression.metadata.value ?: return true
        val templates = configExpression.metadata.snippetTemplates ?: return true
        if (templates.isEmpty()) return true

        val value = element.value
        val project = config.configGroup.project
        val type = typeExpression.substringBefore('.') // 匹配定义时忽略子类型
        val selector = ParadoxDefinitionSearch.selector(project, element).contextSensitive()
        val missingNames = templates.mapNotNullFast { template ->
            val fullName = template.resolve(value)
            if (ParadoxDefinitionSearch.search(fullName, type, selector).findFirst() != null) null else fullName
        }
        if (missingNames.isEmpty()) return true

        val description = when {
            context.showExpect -> ChronicleEpBundle.message("incorrectExpression.definitionSnippet.desc.1", missingNames.joinToStringFast())
            else -> ChronicleEpBundle.message("incorrectExpression.definitionSnippet.desc.0")
        }
        context.holder.registerProblem(element, description)
        return false
    }
}

/**
 * 检查本地化引用片段（[CwtDataTypes.LocalisationSnippet]）是否完全匹配。
 *
 * 说明：
 * - 语义匹配阶段采用宽松策略，仅要求至少一个模板参数能够解析为存在的本地化。
 * - 此检查器要求所有模板参数都能解析为存在的本地化，否则报告“部分匹配”。
 *
 * @see CwtDataTypes.LocalisationSnippet
 */
class ParadoxLocalisationSnippetChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, context: ParadoxExpressionInspectionContext): Boolean {
        val configExpression = ProcessorScope.findFrom<CwtDataExpression>({ config.expandConfigExpression { process(it) } }) { it.type == CwtDataTypes.LocalisationSnippet } ?: return true
        val templates = configExpression.metadata.snippetTemplates ?: return true
        if (templates.isEmpty()) return true

        val value = element.value
        val project = config.configGroup.project
        val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
        val missingNames = templates.mapNotNullFast { template ->
            val fullName = template.resolve(value)
            if (ParadoxLocalisationSearch.searchNormal(fullName, selector).findFirst() != null) null else fullName
        }
        if (missingNames.isEmpty()) return true

        val description = when {
            context.showExpect -> ChronicleEpBundle.message("incorrectExpression.localisationSnippet.desc.1", missingNames.joinToStringFast())
            else -> ChronicleEpBundle.message("incorrectExpression.localisationSnippet.desc.0")
        }
        context.holder.registerProblem(element, description)
        return false
    }
}
