package icu.windea.pls.lang.checker

import com.intellij.codeInspection.*
import com.intellij.openapi.util.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

class ParadoxRangedIntChecker: ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxScriptExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        val configExpression = config.expression
        if(config.expression.type != CwtDataType.Int) return
        val expression = element.expression ?: return
        val (min, max) = configExpression.extraValue<Tuple2<Int?, Int?>>() ?: return
        val min0 = min ?: Int.MIN_VALUE
        val max0 = max ?: Int.MAX_VALUE
        val value = element.intValue() ?: return
        if(value !in min0..max0) {
            val min1 = min?.toString() ?: "-inf"
            val max1 = max?.toString() ?: "inf"
            holder.registerProblem(element, PlsBundle.message("inspection.script.general.incorrectExpression.description.1", expression, min1, max1, value))
        }
    }
}

class ParadoxRangedFloatChecker: ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxScriptExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        val configExpression = config.expression
        if(config.expression.type != CwtDataType.Float) return
        val expression = element.expression ?: return
        val (min, max) = configExpression.extraValue<Tuple2<Float?, Float?>>() ?: return
        val min0 = min ?: Float.MIN_VALUE
        val max0 = max ?: Float.MAX_VALUE
        val value = element.floatValue() ?: return
        if(value !in min0..max0) {
            val min1 = min?.toString() ?: "-inf"
            val max1 = max?.toString() ?: "inf"
            holder.registerProblem(element, PlsBundle.message("inspection.script.general.incorrectExpression.description.1", expression, min1, max1, value))
        }
    }
}

class ParadoxColorFieldChecker: ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxScriptExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        val configExpression = config.expression
        if(configExpression.type != CwtDataType.ColorField) return
        val expression = element.expression ?: return
        if(element !is ParadoxScriptColor) return
        val expectedColorType = configExpression.value ?: return
        val colorType = element.colorType
        if(colorType == expectedColorType) return
        val message = PlsBundle.message("inspection.script.general.incorrectExpression.description.3", expression, expectedColorType, colorType)
        holder.registerProblem(element, message)
    }
}

class ParadoxScopeBasedScopeFieldExpressionChecker: ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxScriptExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        val configExpression = config.expression
        if(configExpression.type != CwtDataType.Scope) return
        if(element !is ParadoxScriptStringExpressionElement) return
        val expectedScope = configExpression.value ?: return
        val text = element.text
        val textRange = TextRange.create(0, text.length)
        val configGroup = config.info.configGroup
        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(text, textRange, configGroup) ?: return
        val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = true) ?: return
        val parentScopeContext = ParadoxScopeHandler.getScopeContext(memberElement) ?: return
        val scopeContext = ParadoxScopeHandler.getScopeContext(element, scopeFieldExpression, parentScopeContext)
        if(ParadoxScopeHandler.matchesScope(scopeContext, expectedScope, configGroup)) return
        val expression = element.expression ?: return
        val message = PlsBundle.message("inspection.script.general.incorrectExpression.description.5", expression, expectedScope, scopeContext.scope.id)
        holder.registerProblem(element, message)
    }
}

class ParadoxScopeGroupBasedScopeFieldExpressionChecker: ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxScriptExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        val configExpression = config.expression
        if(configExpression.type != CwtDataType.ScopeGroup) return
        if(element !is ParadoxScriptStringExpressionElement) return
        val expectedScopeGroup = configExpression.value ?: return
        val text = element.text
        val textRange = TextRange.create(0, text.length)
        val configGroup = config.info.configGroup
        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(text, textRange, configGroup) ?: return
        val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = true) ?: return
        val parentScopeContext = ParadoxScopeHandler.getScopeContext(memberElement) ?: return
        val scopeContext = ParadoxScopeHandler.getScopeContext(element, scopeFieldExpression, parentScopeContext)
        if(ParadoxScopeHandler.matchesScopeGroup(scopeContext, expectedScopeGroup, configGroup)) return
        val expression = element.expression ?: return
        val message = PlsBundle.message("inspection.script.general.incorrectExpression.description.6", expression, expectedScopeGroup, scopeContext.scope.id)
        holder.registerProblem(element, message)
    }
}

class ParadoxTriggerInSwitchChecker : ParadoxIncorrectExpressionChecker {
    object Data {
        val TRIGGER_KEYS = arrayOf("trigger", "on_trigger")
        val CONTEXT_NAMES = arrayOf("switch", "inverted_switch")
    }
    
    override fun check(element: ParadoxScriptExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        //switch = {...}和inverted_switch = {...}中指定的应当是一个simple_trigger
        if(element !is ParadoxScriptString && element !is ParadoxScriptScriptedVariableReference) return
        if(config !is CwtValueConfig || config.value != "alias_keys_field[trigger]") return
        
        val propertyConfig = config.propertyConfig ?: return
        if(propertyConfig.key !in Data.TRIGGER_KEYS) return
        val aliasConfig = config.memberConfig.parentConfig?.castOrNull<CwtPropertyConfig>()?.inlineableConfig?.castOrNull<CwtAliasConfig>() ?: return
        if(aliasConfig.subName !in Data.CONTEXT_NAMES) return
        
        val triggerName = element.stringValue() ?: return
        val configGroup = config.info.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.orNull() ?: return
        if(resultTriggerConfigs.none { !it.config.isBlock }) {
            holder.registerProblem(element, PlsBundle.message("incorrectExpressionChecker.expect.simpleTrigger", element.expression.orEmpty()))
        }
    }
}

class ParadoxTriggerInTriggerWithParametersAwareChecker : ParadoxIncorrectExpressionChecker {
    object Data {
        const val TRIGGER_KEY = "trigger"
        val CONTEXT_NAMES = arrayOf("complex_trigger_modifier", "export_trigger_value_to_variable")
    }
    
    override fun check(element: ParadoxScriptExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        //complex_trigger_modifier = {...}中指定的应当是一个simple_trigger（如果不带参数）或者complex_trigger（如果带参数）
        if(element !is ParadoxScriptString && element !is ParadoxScriptScriptedVariableReference) return
        if(config !is CwtValueConfig || config.value != "alias_keys_field[trigger]") return
        
        val propertyConfig = config.propertyConfig ?: return
        if(propertyConfig.key != Data.TRIGGER_KEY) return
        val aliasConfig = propertyConfig.parentConfig?.castOrNull<CwtPropertyConfig>()?.inlineableConfig?.castOrNull<CwtAliasConfig>() ?: return
        if(aliasConfig.subName !in Data.CONTEXT_NAMES) return
        
        val triggerName = element.stringValue() ?: return
        val configGroup = config.info.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.orNull() ?: return
        if(hasParameters(element)) {
            if(resultTriggerConfigs.none { it.config.isBlock }) {
                holder.registerProblem(element, PlsBundle.message("incorrectExpressionChecker.expect.complexTrigger", element.expression.orEmpty()))
            }
        } else {
            //can also be complex trigger here, for some parameters can be ignored (like "count = xxx")
            //if(resultTriggerConfigs.none { !it.config.isBlock }) {
            //    holder.registerProblem(element, PlsBundle.message("incorrectExpressionChecker.expect.simpleTrigger", element.expression.orEmpty()))
            //}
        }
    }
    
    private fun hasParameters(element: ParadoxScriptExpressionElement) =
        element.findParentProperty()?.findParentProperty()?.findProperty("parameters") != null
}