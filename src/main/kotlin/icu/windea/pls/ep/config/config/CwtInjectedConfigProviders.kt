package icu.windea.pls.ep.config.config

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.declarationConfigContext
import icu.windea.pls.config.config.parents
import icu.windea.pls.config.manipulation.CwtConfigScopeAwareManipulationService
import icu.windea.pls.lang.resolve.onActionConfig
import icu.windea.pls.lang.util.ParadoxEventManager
import icu.windea.pls.model.ParadoxGameType

class CwtInOnActionInjectedConfigProvider : CwtExpressionStringBasedInjectedConfigProvider() {
    // 如果可以确定 on_action 的事件类型，直接位于其中的 `<event>` 需要替换为 `<event.scopeless>` 和 `<event.{eventType}>`，其中 `{eventType}` 为事件类型
    // 2.1.10 #344 对于基于 Jomini 的游戏类型，需要排除 `effect = {...}`（以及 `trigger = {...}`）中的 `<event>` - 宽松检测：实现代码中不检测游戏类型

    private val logger = thisLogger()
    private val expression = "<event>"
    private fun expression(eventType: String) = "<event.$eventType>"
    private val expressionScopeless = "<event.scopeless>"

    override fun doInject(parentConfig: CwtMemberConfig<*>, config: CwtMemberConfig<*>, expressionString: String): List<String>? {
        if (expressionString != expression) return null
        val rootConfig = config.parents(withSelf = false).lastOrNull() ?: return null
        val declarationConfigContext = rootConfig.declarationConfigContext ?: return null
        val onActionConfig = declarationConfigContext.onActionConfig ?: return null
        val eventType = onActionConfig.eventType
        if (eventType.isEmpty() || eventType == "scopeless") return null // ignore
        val allEventTypes = ParadoxEventManager.getAllTypes(config.configGroup.gameType)
        if (eventType !in allEventTypes) {
            logger.warn("Applied config injection in declaration of on action `${onActionConfig.name}` failed: unknown event type `$eventType`")
            return null
        }
        val withinTriggerOrEffectClause = CwtConfigScopeAwareManipulationService.withinTriggerOrEffectClause(config) // lenient check
        if (withinTriggerOrEffectClause) return null
        val result = buildList {
            if ("scopeless" in allEventTypes) this += expressionScopeless
            this += expression(eventType)
        }
        logger.debug { "Applied config injection in declaration of on action `${onActionConfig.name}`: replace `$expression` with ${result.joinToString()}" }
        return result
    }

    override fun keepOrigin(config: CwtMemberConfig<*>) = false
}

class CwtTechnologyWithLevelInjectedConfigProvider : CwtExpressionStringBasedInjectedConfigProvider() {
    // 如果 Stellaris 中的脚本表达式至少匹配 `<technology.repeatable>`，则也可以匹配 `$technology_with_level`
    // https://github.com/cwtools/cwtools-vscode/issues/58

    private val logger = thisLogger()
    private val expressions = listOf("<technology>", "<technology.repeatable>")
    private val injectedExpressions = listOf("\$technology_with_level")

    override fun supports(gameType: ParadoxGameType) = gameType == ParadoxGameType.Stellaris

    override fun doInject(parentConfig: CwtMemberConfig<*>, config: CwtMemberConfig<*>, expressionString: String): List<String>? {
        if (expressionString !in expressions) return null
        val result = injectedExpressions
        logger.debug { "Applied config injection: replace `${expressionString}` with ${result.joinToString()}`" }
        return result
    }
}
