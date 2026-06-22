package icu.windea.pls.ep.config.config

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.util.CwtMemberConfigVisitor
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.forEachReversedIndexedFast

/**
 * 用于基于规则表达式字符串注入规则。
 */
abstract class CwtExpressionStringBasedInjectedConfigProvider : CwtInjectedConfigProvider {
    @Optimized
    override fun injectConfigs(parentConfig: CwtMemberConfig<*>, containerConfig: CwtMemberConfig<*>, configs: MutableList<CwtMemberConfig<*>>): Boolean {
        // 2.1.1 通过使用访问者模式，消除类型检查
        var r = false
        configs.forEachReversedIndexedFast { i, config ->
            config.accept(object : CwtMemberConfigVisitor() {
                override fun visitProperty(config: CwtPropertyConfig): Boolean {
                    val key = config.key
                    val value = config.value
                    val injectedKeys = doInjectKey(parentConfig, config, key)
                    val injectedValues = doInjectValue(parentConfig, config, value)
                    val injected = injectedKeys != null || injectedValues != null
                    r = r || injected
                    if (!injected) return true
                    var i0 = i + 1
                    (injectedKeys ?: listOf(key)).forEachFast { injectedKey ->
                        (injectedValues ?: listOf(value)).forEachFast { injectedValue ->
                            val delegatedConfig = config.delegatedWith(injectedKey, injectedValue).also { it.parentConfig = containerConfig }
                            configs.add(i0, delegatedConfig)
                            i0++
                        }
                    }
                    if (!keepOrigin(config)) configs.removeAt(i)
                    return true
                }

                override fun visitValue(config: CwtValueConfig): Boolean {
                    val value = config.value
                    val injectedValues = doInjectValue(parentConfig, config, value)
                    val injected = injectedValues != null
                    r = r || injected
                    if (!injected) return true
                    var i0 = i + 1
                    injectedValues.forEachFast { injectedValue ->
                        val delegatedConfig = config.delegatedWith(injectedValue).also { it.parentConfig = containerConfig }
                        configs.add(i0, delegatedConfig)
                        i0++
                    }
                    if (!keepOrigin(config)) configs.removeAt(i)
                    return true
                }
            })
        }
        return r
    }

    /**
     * 对键或值进行注入，返回得到的表达式字符串列表。如果为 null，则表示不进行注入。
     *
     * @param parentConfig 原始的父规则。
     * @param config 当前正在遍历的规则。
     * @param expressionString 当前正在遍历的规则表达式字符串。
     */
    protected open fun doInject(parentConfig: CwtMemberConfig<*>, config: CwtMemberConfig<*>, expressionString: String): List<String>? = null

    /**
     * 对键进行注入，返回得到的表达式字符串列表。如果为 null，则表示不进行注入。
     *
     * @param parentConfig 原始的父规则。
     * @param config 当前正在遍历的规则。
     * @param expressionString 当前正在遍历的规则表达式字符串。
     */
    protected open fun doInjectKey(parentConfig: CwtMemberConfig<*>, config: CwtMemberConfig<*>, expressionString: String): List<String>? = doInject(parentConfig, config, expressionString)

    /**
     * 对值进行注入，返回得到的表达式字符串列表。如果为 null，则表示不进行注入。
     *
     * @param parentConfig 原始的父规则。
     * @param config 当前正在遍历的规则。
     * @param expressionString 当前正在遍历的规则表达式字符串。
     */
    protected open fun doInjectValue(parentConfig: CwtMemberConfig<*>, config: CwtMemberConfig<*>, expressionString: String): List<String>? = doInject(parentConfig, config, expressionString)

    /**
     * 是否需要保留原始规则。
     */
    protected open fun keepOrigin(config: CwtMemberConfig<*>) = true
}
