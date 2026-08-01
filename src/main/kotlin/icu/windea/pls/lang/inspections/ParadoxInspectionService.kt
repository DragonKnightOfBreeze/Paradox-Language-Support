package icu.windea.pls.lang.inspections

import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.expandConfigExpression
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.findFast
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.util.ProcessorScope
import icu.windea.pls.ep.inspections.ParadoxDefinitionInspectionSuppressionProvider
import icu.windea.pls.ep.inspections.ParadoxIncorrectExpressionChecker
import icu.windea.pls.ep.inspections.ParadoxIncorrectSyntaxChecker
import icu.windea.pls.ep.inspections.ParadoxUnresolvedExpressionChecker
import icu.windea.pls.lang.match.findByPattern
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.constants.ParadoxDefinitionTypes
import icu.windea.pls.model.expressions.ParadoxDefinitionTypeExpression
import icu.windea.pls.model.orSpecific
import icu.windea.pls.script.psi.ParadoxDefinitionElement

@Optimized
object ParadoxInspectionService {
    fun getSuppressedToolIds(definition: ParadoxDefinitionElement, definitionInfo: ParadoxDefinitionInfo): Set<String> {
        val gameType = definitionInfo.gameType
        val result = mutableSetOf<String>()
        val eps = ParadoxDefinitionInspectionSuppressionProvider.EP_NAME.extensionList
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            result += ep.getSuppressedToolIds(definition, definitionInfo)
        }
        return result
    }

    fun checkIncorrectSyntax(element: PsiElement, context: ParadoxSyntaxInspectionContext, eps: List<ParadoxIncorrectSyntaxChecker>): Boolean {
        val gameType = context.gameType
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            val r = ep.check(element, context)
            if (!r) return false
        }
        return true
    }

    fun checkIncorrectExpression(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, context: ParadoxExpressionInspectionContext, eps: List<ParadoxIncorrectExpressionChecker>): Boolean {
        val gameType = context.gameType
        eps.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            val r = ep.check(element, config, context)
            if (!r) return false
        }
        return true
    }

    fun checkUnresolvedExpression(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>, context: ParadoxExpressionInspectionContext, checkers: List<ParadoxUnresolvedExpressionChecker>): Boolean {
        val gameType = context.gameType
        checkers.forEachFast f@{ ep ->
            if (gameType.orSpecific() != null && !ep.supports(gameType)) return@f // check game type first
            val r = ep.check(element, expectedConfigs, context)
            if (!r) return false
        }
        return true
    }

    fun checkExtendedConfig(element: ParadoxExpressionElement, expectedConfigs: List<CwtMemberConfig<*>>): Boolean {
        if (expectedConfigs.isEmpty()) return false
        val value = element.value
        val configGroup = expectedConfigs.first().configGroup
        return ProcessorScope.anyFrom({ expectedConfigs.expandConfigExpression { process(it) } }) { doCheckExtendedConfig(element, value, it, configGroup) }
    }

    private fun doCheckExtendedConfig(element: ParadoxExpressionElement, value: String, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Boolean {
        if (configExpression.type in CwtDataTypeSets.DefinitionAware) {
            val definitionType = configExpression.metadata.value ?: return false
            val configs = configGroup.extendedDefinitions.findByPattern(value, element, configGroup).orEmpty()
            val config = configs.findFast { ParadoxDefinitionTypeExpression.resolve(it.type).matches(definitionType) }
            if (config != null) return true
            if (definitionType == ParadoxDefinitionTypes.gameRule) {
                val config = configGroup.extendedGameRules.findByPattern(value, element, configGroup)
                if (config != null) return true
            }
            if (definitionType == ParadoxDefinitionTypes.onAction) {
                val config = configGroup.extendedOnActions.findByPattern(value, element, configGroup)
                if (config != null) return true
            }
        }
        return false
    }
}
