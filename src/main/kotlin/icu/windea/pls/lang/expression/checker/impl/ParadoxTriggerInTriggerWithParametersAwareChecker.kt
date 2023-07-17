package icu.windea.pls.lang.expression.checker.impl

import com.intellij.codeInspection.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.expression.checker.*
import icu.windea.pls.script.psi.*

class ParadoxTriggerInTriggerWithParametersAwareChecker : ParadoxIncorrectExpressionChecker {
    companion object {
        private const val TRIGGER_KEY = "trigger"
        private val CONTEXT_NAMES = arrayOf("complex_trigger_modifier", "export_trigger_value_to_variable")
        
    }
    
    override fun check(element: ParadoxScriptExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        //complex_trigger_modifier = {...}中指定的应当是一个simple_trigger（如果不带参数）或者complex_trigger（如果带参数）
        if(element !is ParadoxScriptString && element !is ParadoxScriptScriptedVariableReference) return
        if(config !is CwtValueConfig || config.value != "alias_keys_field[trigger]") return
        
        val propertyConfig = config.propertyConfig ?: return
        if(propertyConfig.key != TRIGGER_KEY) return
        val aliasConfig = propertyConfig.parent?.castOrNull<CwtPropertyConfig>()?.inlineableConfig?.castOrNull<CwtAliasConfig>() ?: return
        if(aliasConfig.subName !in CONTEXT_NAMES) return
        
        val triggerName = element.stringValue() ?: return
        val configGroup = config.info.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.takeIfNotEmpty() ?: return
        if(hasParameters(element)) {
            if(resultTriggerConfigs.none { it.config.isBlock }) {
                holder.registerProblem(element, PlsBundle.message("incorrectExpressionChecker.expect.complexTrigger", element.expression.orEmpty()))
            }
        } else {
            if(resultTriggerConfigs.none { !it.config.isBlock }) {
                holder.registerProblem(element, PlsBundle.message("incorrectExpressionChecker.expect.simpleTrigger", element.expression.orEmpty()))
            }
        }
    }
    
    private fun hasParameters(element: ParadoxScriptExpressionElement) =
        element.findParentProperty()?.findParentProperty()?.findProperty("parameters") != null
}
