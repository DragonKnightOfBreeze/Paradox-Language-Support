package icu.windea.pls.ep.config.config

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig

/**
 * 用于基于表达式字符串注入规则。
 */
abstract class CwtExpressionStringBasedInjectedConfigProvider : CwtInjectedConfigProvider {
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
                    val injected = injectedKeys != null || injectedValues != null
                    r = r || injected
                    if (!injected) continue
                    var i0 = i + 1
                    (injectedKeys ?: listOf(key)).forEach { injectedKey ->
                        (injectedValues ?: listOf(value)).forEach { injectedValue ->
                            val delegatedConfig = CwtPropertyConfig.delegatedWith(config, injectedKey, injectedValue).also { it.parentConfig = parentConfig }
                            configs.add(i0, delegatedConfig)
                            i0++
                        }
                    }
                    if (!keepOrigin(config)) configs.removeAt(i)
                }
                is CwtValueConfig -> {
                    val value = config.value
                    val injectedValues = doInjectValue(config, value)
                    val injected = injectedValues != null
                    r = r || injected
                    if (!injected) continue
                    var i0 = i + 1
                    injectedValues.forEach { injectedValue ->
                        val delegatedConfig = CwtValueConfig.delegatedWith(config, injectedValue).also { it.parentConfig = parentConfig }
                        configs.add(i0, delegatedConfig)
                        i0++
                    }
                    if (!keepOrigin(config)) configs.removeAt(i)
                }
            }
        }
        return r
    }

    /**
     * 对键或值进行注入，返回得到的表达式字符串列表。如果为 null，则表示不进行注入。
     */
    protected open fun doInject(config: CwtMemberConfig<*>, expressionString: String): List<String>? = null

    /**
     * 对键进行注入，返回得到的表达式字符串列表。如果为 null，则表示不进行注入。
     */
    protected open fun doInjectKey(config: CwtMemberConfig<*>, expressionString: String): List<String>? {
        return doInject(config, expressionString)
    }

    /**
     * 对值进行注入，返回得到的表达式字符串列表。如果为 null，则表示不进行注入。
     */
    protected open fun doInjectValue(config: CwtMemberConfig<*>, expressionString: String): List<String>? {
        return doInject(config, expressionString)
    }

    /**
     * 是否需要保留原始规则。
     */
    protected open fun keepOrigin(config: CwtMemberConfig<*>) = true
}
