package icu.windea.pls.ep.inspections

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.expandConfigExpression
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.filterFast
import icu.windea.pls.core.collections.mapNotNullFast
import icu.windea.pls.core.joinToStringFast
import icu.windea.pls.core.util.ProcessorScope
import icu.windea.pls.ep.ChronicleEpBundle
import icu.windea.pls.lang.inspections.ParadoxExpressionInspectionContext
import icu.windea.pls.lang.match.util.ParadoxMatchFactory
import icu.windea.pls.lang.psi.ParadoxExpressionElement

/**
 * 检查定义引用片段（[CwtDataTypes.DefinitionSnippet]）是否完全匹配。
 *
 * 说明：
 * - 语义匹配阶段采用宽松策略，仅要求至少一个模板参数能够解析为对应类型的定义。
 * - 此检查器要求所有模板参数都能解析为对应类型的定义，否则报告“部分匹配”。
 * - 匹配定义时不忽略子类型。
 *
 * @see CwtDataTypes.DefinitionSnippet
 */
@Optimized
class ParadoxDefinitionSnippetChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, context: ParadoxExpressionInspectionContext): Boolean {
        val configExpression = ProcessorScope.findFrom({ config.expandConfigExpression { process(it) } }) { it.type == CwtDataTypes.DefinitionSnippet }
        if (configExpression == null) return true

        val typeExpression = configExpression.metadata.value
        if (typeExpression == null) return true
        val templates = configExpression.metadata.snippetTemplates
        if (templates.isNullOrEmpty()) return true
        val value = element.value
        val project = config.configGroup.project
        val fullNames = templates.mapNotNullFast { it.resolve(value) }
        val missingNames = fullNames.filterFast { !ParadoxMatchFactory.matchesDefinition(element, project, it, typeExpression) }
        if (missingNames.size == fullNames.size) return true // require partially matched first
        if (missingNames.isEmpty()) return true // skip if full matched

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
 * - 语义匹配阶段采用宽松策略，仅要求至少一个模板参数能够解析为本地化。
 * - 此检查器要求所有模板参数都能解析为本地化，否则报告“部分匹配”。
 *
 * @see CwtDataTypes.LocalisationSnippet
 */
@Optimized
class ParadoxLocalisationSnippetChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, context: ParadoxExpressionInspectionContext): Boolean {
        val configExpression = ProcessorScope.findFrom({ config.expandConfigExpression { process(it) } }) { it.type == CwtDataTypes.LocalisationSnippet }
        if (configExpression == null) return true

        val templates = configExpression.metadata.snippetTemplates
        if (templates.isNullOrEmpty()) return true
        val value = element.value
        val project = config.configGroup.project
        val fullNames = templates.mapNotNullFast { it.resolve(value) }
        val missingNames = fullNames.filterFast { !ParadoxMatchFactory.matchesLocalisation(element, project, it) }
        if (missingNames.size == fullNames.size) return true // require partially matched first
        if (missingNames.isEmpty()) return true // skip if full matched

        val description = when {
            context.showExpect -> ChronicleEpBundle.message("incorrectExpression.localisationSnippet.desc.1", missingNames.joinToStringFast())
            else -> ChronicleEpBundle.message("incorrectExpression.localisationSnippet.desc.0")
        }
        context.holder.registerProblem(element, description)
        return false
    }
}
