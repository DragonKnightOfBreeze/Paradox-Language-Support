package icu.windea.pls.ep.config

import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

abstract class ExpressionStringBasedCwtInjectedConfigProvider : CwtInjectedConfigProvider {
    override fun injectConfigs(parentConfig: CwtMemberConfig<*>, configs: MutableList<CwtMemberConfig<*>>): Boolean {
        var r = false
        for (i in configs.lastIndex downTo 0) {
            val config = configs[i]
            when (config) {
                is CwtPropertyConfig -> {
                    val key = config.key
                    val value = config.value
                    val injectedKeys = doInjectKey(config, key)
                    val injectedValues = doInjectValue(config, value)
                    r = r || (injectedKeys != null || injectedValues != null)
                    if (injectedKeys == null && injectedValues == null) continue
                    var i0 = i + 1
                    (injectedKeys ?: listOf(key)).forEach { injectedKey ->
                        (injectedValues ?: listOf(value)).forEach { injectedValue ->
                            configs.add(i0, config.delegatedWith(injectedKey, injectedValue))
                            i0++
                        }
                    }
                    if (!keepOrigin(config)) configs.removeAt(i)
                }
                is CwtValueConfig -> {
                    val value = config.value
                    val injectedValues = doInjectValue(config, value)
                    r = r || injectedValues != null
                    if (injectedValues == null) continue
                    var i0 = i + 1
                    injectedValues.forEach { injectedValue ->
                        configs.add(i0, config.delegatedWith(injectedValue))
                        i0++
                    }
                    if (!keepOrigin(config)) configs.removeAt(i)
                }
            }
        }
        return r
    }

    /**
     * @return 注入后得到的表达式字符串列表。如果为null，则表示不进行注入。
     */
    protected open fun doInject(config: CwtMemberConfig<*>, expressionString: String): List<String>? {
        return null
    }

    /**
     * @return 注入后得到的表达式字符串列表。如果为null，则表示不进行注入。
     */
    protected open fun doInjectKey(config: CwtMemberConfig<*>, expressionString: String): List<String>? {
        return doInject(config, expressionString)
    }

    /**
     * @return 注入后得到的表达式字符串列表。如果为null，则表示不进行注入。
     */
    protected open fun doInjectValue(config: CwtMemberConfig<*>, expressionString: String): List<String>? {
        return doInject(config, expressionString)
    }

    /**
     * @return 是否需要保留原始的CWT规则。
     */
    protected open fun keepOrigin(config: CwtMemberConfig<*>): Boolean {
        return true
    }
}

@WithGameType(ParadoxGameType.Stellaris)
class CwtTechnologyWithLevelInjectedConfigProvider : ExpressionStringBasedCwtInjectedConfigProvider() {
    private val expressions = listOf("<technology>", "<technology.repeatable>")
    private val injectedExpressions = listOf("<technology_with_level>")

    override fun doInject(config: CwtMemberConfig<*>, expressionString: String): List<String>? {
        //如果Stellaris中的脚本表达式至少匹配"<technology.repeatable>"，它也可以匹配"<technology_with_level>"
        //https://github.com/cwtools/cwtools-vscode/issues/58

        if (expressionString !in expressions) return null
        return injectedExpressions
    }
}

class CwtInOnActionInjectedConfigProvider : ExpressionStringBasedCwtInjectedConfigProvider() {
    override fun doInject(config: CwtMemberConfig<*>, expressionString: String): List<String>? {
        //如果预定义的on_action可以确定事件类型，直接位于其中的"<event>"需要替换为此事件类型对应的规则

        if (expressionString != "<event>") return null
        var currentConfig = config.memberConfig
        while (true) {
            currentConfig = currentConfig.parentConfig ?: break
        }
        val declarationConfigContext = currentConfig.declarationConfigContext ?: return null
        val onActionConfig = declarationConfigContext.onActionConfig ?: return null
        if (onActionConfig.eventType == ParadoxScopeManager.anyScopeId) return null //ignore
        val configGroup = declarationConfigContext.configGroup
        return buildList {
            if (configGroup.types.get("event")?.subtypes?.containsKey("scopeless") == true) {
                this += "<event.scopeless>"
            }
            this += "<event.${onActionConfig.eventType}>"
        }
    }

    override fun keepOrigin(config: CwtMemberConfig<*>): Boolean {
        return false
    }
}
