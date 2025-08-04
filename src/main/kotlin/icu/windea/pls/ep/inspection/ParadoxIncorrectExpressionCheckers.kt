package icu.windea.pls.ep.inspection

import com.intellij.codeInspection.*
import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.lang.codeInsight.*
import icu.windea.pls.lang.expression.complex.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

class ParadoxRangedIntChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        if (element !is ParadoxScriptExpressionElement && element !is ParadoxCsvExpressionElement) return

        val configExpression = config.configExpression
        if (configExpression.type != CwtDataTypes.Int) return //for int only
        val expression = element.expression
        val (min0, max0) = configExpression.intRange ?: return
        val value = when {
            element is ParadoxScriptExpressionElement -> element.resolved()?.intValue()
            element is ParadoxCsvExpressionElement -> element.value.toIntOrNull()
            else -> null
        }
        if (value == null) return
        val min = min0 ?: Int.MIN_VALUE
        val max = max0 ?: Int.MAX_VALUE
        if (value !in min..max) {
            val min1 = min0?.toString() ?: "-inf"
            val max1 = max0?.toString() ?: "inf"
            holder.registerProblem(element, PlsBundle.message("incorrectExpressionChecker.expect.range", expression, min1, max1, value))
        }
    }
}

class ParadoxRangedFloatChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        if (element !is ParadoxScriptExpressionElement && element !is ParadoxCsvExpressionElement) return

        val configExpression = config.configExpression
        if (configExpression.type != CwtDataTypes.Int && configExpression.type != CwtDataTypes.Float) return //for int and float
        val expression = element.expression
        val (min0, max0) = configExpression.floatRange ?: return
        val value = when {
            element is ParadoxScriptExpressionElement -> element.resolved()?.floatValue()
            element is ParadoxCsvExpressionElement -> element.value.toFloatOrNull()
            else -> null
        }
        if (value == null) return
        val min = min0 ?: Float.MIN_VALUE
        val max = max0 ?: Float.MAX_VALUE
        if (value !in min..max) {
            val min1 = min0?.toString() ?: "-inf"
            val max1 = max0?.toString() ?: "inf"
            holder.registerProblem(element, PlsBundle.message("incorrectExpressionChecker.expect.range", expression, min1, max1, value))
        }
    }
}

class ParadoxColorFieldChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        if (element !is ParadoxScriptColor) return

        val configExpression = config.configExpression
        if (configExpression.type != CwtDataTypes.ColorField) return
        val expression = element.expression
        val expectedColorType = configExpression.value ?: return
        val colorType = element.colorType
        if (colorType == expectedColorType) return
        val message = PlsBundle.message("incorrectExpressionChecker.expect.colorType", expression, expectedColorType, colorType)
        holder.registerProblem(element, message)
    }
}

class ParadoxScopeBasedScopeFieldExpressionChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        if (element !is ParadoxScriptStringExpressionElement) return

        val configExpression = config.configExpression
        if (configExpression.type != CwtDataTypes.Scope) return
        val expectedScope = configExpression.value ?: return
        val value = element.value
        val textRange = TextRange.create(0, value.length)
        val configGroup = config.configGroup
        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(value, textRange, configGroup) ?: return
        val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = true) ?: return
        val parentScopeContext = ParadoxScopeManager.getSwitchedScopeContext(memberElement) ?: ParadoxScopeManager.getAnyScopeContext()
        val scopeContext = ParadoxScopeManager.getSwitchedScopeContext(element, scopeFieldExpression, parentScopeContext)
        if (ParadoxScopeManager.matchesScope(scopeContext, expectedScope, configGroup)) return
        val expression = element.expression
        val message = PlsBundle.message("incorrectExpressionChecker.expect.scope", expression, expectedScope, scopeContext.scope.id)
        holder.registerProblem(element, message)
    }
}

class ParadoxScopeGroupBasedScopeFieldExpressionChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        if (element !is ParadoxScriptStringExpressionElement) return

        val configExpression = config.configExpression
        if (configExpression.type != CwtDataTypes.ScopeGroup) return
        val expectedScopeGroup = configExpression.value ?: return
        val value = element.value
        val textRange = TextRange.create(0, value.length)
        val configGroup = config.configGroup
        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(value, textRange, configGroup) ?: return
        val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = true) ?: return
        val parentScopeContext = ParadoxScopeManager.getSwitchedScopeContext(memberElement) ?: ParadoxScopeManager.getAnyScopeContext()
        val scopeContext = ParadoxScopeManager.getSwitchedScopeContext(element, scopeFieldExpression, parentScopeContext)
        if (ParadoxScopeManager.matchesScopeGroup(scopeContext, expectedScopeGroup, configGroup)) return
        val expression = element.expression
        val message = PlsBundle.message("incorrectExpressionChecker.expect.scopeGroup", expression, expectedScopeGroup, scopeContext.scope.id)
        holder.registerProblem(element, message)
    }
}

@WithGameType(ParadoxGameType.Stellaris)
class StellarisTechnologyWithLevelChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        if (element !is ParadoxScriptStringExpressionElement) return

        val configExpression = config.configExpression
        if (configExpression.type != CwtDataTypes.TechnologyWithLevel) return
        val (technologyName, technologyLevel) = element.value.split('@', limit = 2).takeIf { it.size == 2 } ?: return
        val project = config.configGroup.project
        val text = element.text
        val separatorIndex = text.indexOf('@')
        if (technologyName.isEmpty() || ParadoxDefinitionSearch.search(technologyName, "technology.repeatable", selector(project, element).definition()).findFirst() == null) {
            val range = TextRange.create(0, text.length).unquote(text).let { TextRange.create(it.startOffset, separatorIndex) }
            val message = PlsBundle.message("incorrectExpressionChecker.expect.repeatableTechnologyName", range.substring(text))
            holder.registerProblem(element, message, ProblemHighlightType.ERROR, range)
        }
        if (technologyLevel.isEmpty() || !technologyLevel.all { c -> c.isExactDigit() } || technologyLevel.toInt() !in -1..100) {
            val range = TextRange.create(0, text.length).unquote(text).let { TextRange.create(separatorIndex + 1, it.endOffset) }
            val message = PlsBundle.message("incorrectExpressionChecker.expect.repeatableTechnologyLevel", range.substring(text))
            holder.registerProblem(element, message, ProblemHighlightType.GENERIC_ERROR, range)
        }
    }
}

class ParadoxTriggerInSwitchChecker : ParadoxIncorrectExpressionChecker {
    object Constants {
        val TRIGGER_KEYS = arrayOf("trigger", "on_trigger")
        val CONTEXT_NAMES = arrayOf("switch", "inverted_switch")
    }

    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        if (element !is ParadoxScriptExpressionElement) return

        //switch = {...}和inverted_switch = {...}中指定的应当是一个simple_trigger
        if (element !is ParadoxScriptString && element !is ParadoxScriptScriptedVariableReference) return
        if (config !is CwtValueConfig || config.value != "alias_keys_field[trigger]") return

        val propertyConfig = config.propertyConfig ?: return
        if (propertyConfig.key !in Constants.TRIGGER_KEYS) return
        val aliasConfig = config.memberConfig.parentConfig?.castOrNull<CwtPropertyConfig>()?.aliasConfig ?: return
        if (aliasConfig.subName !in Constants.CONTEXT_NAMES) return

        val triggerName = element.stringValue() ?: return
        val configGroup = config.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.orNull() ?: return
        if (resultTriggerConfigs.none { !it.config.isBlock }) {
            holder.registerProblem(element, PlsBundle.message("incorrectExpressionChecker.expect.simpleTrigger", element.expression))
        }
    }
}

class ParadoxTriggerInTriggerWithParametersAwareChecker : ParadoxIncorrectExpressionChecker {
    object Constants {
        const val TRIGGER_KEY = "trigger"
        val CONTEXT_NAMES = arrayOf("complex_trigger_modifier", "export_trigger_value_to_variable")
    }

    override fun check(element: ParadoxExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        if (element !is ParadoxScriptExpressionElement) return

        //complex_trigger_modifier = {...}中指定的应当是一个simple_trigger（如果不带参数）或者complex_trigger（如果带参数）
        if (element !is ParadoxScriptString && element !is ParadoxScriptScriptedVariableReference) return
        if (config !is CwtValueConfig || config.value != "alias_keys_field[trigger]") return

        val propertyConfig = config.propertyConfig ?: return
        if (propertyConfig.key != Constants.TRIGGER_KEY) return
        val aliasConfig = propertyConfig.parentConfig?.castOrNull<CwtPropertyConfig>()?.aliasConfig ?: return
        if (aliasConfig.subName !in Constants.CONTEXT_NAMES) return

        val triggerName = element.stringValue() ?: return
        val configGroup = config.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.orNull() ?: return
        val hasParameters = element.findParentProperty()?.findParentProperty()?.findProperty("parameters") != null
        if (hasParameters) {
            if (resultTriggerConfigs.none { it.config.isBlock }) {
                holder.registerProblem(element, PlsBundle.message("incorrectExpressionChecker.expect.complexTrigger", element.expression))
            }
        } else {
            //can also be complex trigger here, for some parameters can be ignored (like "count = xxx")
            //if(resultTriggerConfigs.none { !it.config.isBlock }) {
            //    holder.registerProblem(element, PlsBundle.message("incorrectExpressionChecker.expect.simpleTrigger", element.expression.orEmpty()))
            //}
        }
    }
}
