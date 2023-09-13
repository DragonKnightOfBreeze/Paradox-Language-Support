package icu.windea.pls.lang.expressionChecker.impl

import com.intellij.codeInspection.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.expressionChecker.*
import icu.windea.pls.script.psi.*

private val TRIGGER_KEYS = arrayOf("trigger", "on_trigger")
private val CONTEXT_NAMES = arrayOf("switch", "inverted_switch")

class ParadoxTriggerInSwitchChecker : ParadoxIncorrectExpressionChecker {
    override fun check(element: ParadoxScriptExpressionElement, config: CwtMemberConfig<*>, holder: ProblemsHolder) {
        //switch = {...}和inverted_switch = {...}中指定的应当是一个simple_trigger
        if(element !is ParadoxScriptString && element !is ParadoxScriptScriptedVariableReference) return
        if(config !is CwtValueConfig || config.value != "alias_keys_field[trigger]") return
        
        val propertyConfig = config.propertyConfig ?: return
        if(propertyConfig.key !in TRIGGER_KEYS) return
        val aliasConfig = config.memberConfig.parentConfig?.castOrNull<CwtPropertyConfig>()?.inlineableConfig?.castOrNull<CwtAliasConfig>() ?: return
        if(aliasConfig.subName !in CONTEXT_NAMES) return
        
        val triggerName = element.stringValue() ?: return
        val configGroup = config.info.configGroup
        val resultTriggerConfigs = configGroup.aliasGroups.get("trigger")?.get(triggerName)?.orNull() ?: return
        if(resultTriggerConfigs.none { !it.config.isBlock }) {
            holder.registerProblem(element, PlsBundle.message("incorrectExpressionChecker.expect.simpleTrigger", element.expression.orEmpty()))
        }
    }
}