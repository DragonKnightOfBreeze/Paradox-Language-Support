package icu.windea.pls.lang.checker

import com.intellij.codeInspection.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.script.psi.*

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