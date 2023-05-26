package icu.windea.pls.lang.expression.checker.impl

import com.intellij.codeInspection.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.expression.checker.*
import icu.windea.pls.script.psi.*

class ParadoxExpectComplexTriggerModifierTriggerChecker : ParadoxIncorrectExpressionChecker {
    companion object {
        private const val TRIGGER_KEY = "trigger"
        private const val COMPLEX_TRIGGER_MODIFIER_KEY = "alias[modifier_rule:complex_trigger_modifier]"
        
    }
    
    override fun check(element: ParadoxScriptExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        //complex_trigger_modifier = {...}中判定的应当是一个simple_trigger
        if(element !is ParadoxScriptString && element !is ParadoxScriptScriptedVariableReference) return
        if(config !is CwtValueConfig || config.value != "alias_keys_field[trigger]") return
        
        val propertyConfig = config.propertyConfig ?: return
        if(propertyConfig.key != TRIGGER_KEY) return
        val aliasConfig = propertyConfig.parent?.castOrNull<CwtPropertyConfig>()?.inlineableConfig?.castOrNull<CwtAliasConfig>() ?: return
        if(aliasConfig.config.key != COMPLEX_TRIGGER_MODIFIER_KEY) return
        
        val triggerName = element.stringValue() ?: return
        val configGroup = config.info.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.takeIfNotEmpty() ?: return
        if(resultTriggerConfigs.none { it.config.isBlock }) {
            if(hasParameters(element)) {
                holder.registerProblem(element, PlsBundle.message("incorrectExpressionChecker.expect.complexTrigger", element.expression.orEmpty()))
            }
        }
    }
    
    private fun hasParameters(element: ParadoxScriptExpressionElement) =
        element.findParentProperty()?.findParentProperty()?.findProperty("parameters") != null
}

