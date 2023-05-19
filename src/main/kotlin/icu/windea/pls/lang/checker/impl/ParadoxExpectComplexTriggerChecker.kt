package icu.windea.pls.lang.checker.impl

import com.intellij.codeInspection.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.checker.*
import icu.windea.pls.script.psi.*

class ParadoxExpectComplexTriggerChecker : ParadoxIncorrectExpressionChecker {
    companion object {
        private val TRIGGER_KEYS = arrayOf("trigger", "on_trigger")
        private val SWITCH_KEYS = arrayOf("alias[effect:switch]", "alias[trigger:switch]", "alias[effect:inverted_switch]", "alias[effect:inverted_switch]")
    }
    
    override fun check(element: ParadoxScriptExpressionElement, config: CwtDataConfig<*>, holder: ProblemsHolder) {
        if(config !is CwtPropertyConfig) return
        if(config.value != "alias_keys_field[trigger]") return
        
        if(config.key !in TRIGGER_KEYS) return
        val aliasConfig = config.parent?.castOrNull<CwtPropertyConfig>()?.inlineableConfig?.castOrNull<CwtAliasConfig>() ?: return
        if(aliasConfig.config.key !in SWITCH_KEYS) return
        
        val triggerName = element.stringValue() ?: return
        val configGroup = config.info.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.takeIfNotEmpty() ?: return
        val expect = resultTriggerConfigs.none { it.config.isBlock }
        if(expect) {
            holder.registerProblem(element, PlsBundle.message("incorrectExpressionChecker.expect.complexTrigger", element.expression.orEmpty()))
        }
    }
}