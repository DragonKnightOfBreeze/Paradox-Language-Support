package icu.windea.pls.lang.checker.impl

import com.intellij.codeInspection.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.checker.*
import icu.windea.pls.script.psi.*

class ParadoxExpectSimpleTriggerChecker : ParadoxIncorrectExpressionChecker {
    companion object {
        private val TRIGGER_KEYS = arrayOf("trigger", "on_trigger")
        private val SWITCH_KEYS = arrayOf("alias[effect:switch]", "alias[trigger:switch]", "alias[effect:inverted_switch]", "alias[effect:inverted_switch]")
    }
    
    override fun check(element: ParadoxScriptExpressionElement, config: CwtDataConfig<*>, holder: ProblemsHolder) {
        //switch = {...}中判定的应当是一个simple_trigger
        if(element !is ParadoxScriptString && element !is ParadoxScriptScriptedVariableReference) return
        if(config !is CwtValueConfig || config.value != "alias_keys_field[trigger]") return
        
        val propertyConfig = config.propertyConfig ?: return
        if(propertyConfig.key !in TRIGGER_KEYS) return
        val aliasConfig = config.memberConfig.parent?.castOrNull<CwtPropertyConfig>()?.inlineableConfig?.castOrNull<CwtAliasConfig>() ?: return
        if(aliasConfig.config.key !in SWITCH_KEYS) return
        
        val triggerName = element.stringValue() ?: return
        val configGroup = config.info.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.takeIfNotEmpty() ?: return
        val expect = resultTriggerConfigs.none { !it.config.isBlock }
        if(expect) {
            holder.registerProblem(element, PlsBundle.message("incorrectExpressionChecker.expect.simpleTrigger", element.expression.orEmpty()))
        }
    }
}