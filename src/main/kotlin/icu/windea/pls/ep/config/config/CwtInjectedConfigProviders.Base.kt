package icu.windea.pls.ep.config.config

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.forEachReversedIndexedFast

/**
 * 用于基于规则表达式字符串注入规则。
 */
abstract class CwtExpressionStringBasedInjectedConfigProvider : CwtInjectedConfigProvider {
    @Optimized
    override fun injectConfigs(parentConfig: CwtMemberConfig<*>, containerConfig: CwtMemberConfig<*>, configs: MutableList<CwtMemberConfig<*>>): Boolean {
        // NOTE 3.0.1 optimize: 来自 gemini-3.1-pro：将密集的类型检查改为访问者模式，性能通常会更差，而不是更好
        //  - 现代 JVM 对 `instanceof` 指令做了极度深度的优化，比如内联缓存和分支预测；而访问者模式需要两次虚方法调用，这会查找虚方法表（vtable/itable），且会增加方法栈帧的压栈和出栈开销。
        //  - 总之这里的类型检查不是非常明显的性能热点，时间复杂度仍然是无法避免的，让事情变得简单一点。
        var r = false
        configs.forEachReversedIndexedFast { i, config ->
            when (config) {
                is CwtPropertyConfig -> {
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
                            val delegatedConfig = config.delegatedWith(injectedKey, injectedValue).also { it.withParentConfig(containerConfig) }
                            configs.add(i0, delegatedConfig)
                            i0++
                        }
                    }
                    if (!keepOrigin(config)) configs.removeAt(i)
                }
                is CwtValueConfig -> {
                    val value = config.value
                    val injectedValues = doInjectValue(parentConfig, config, value)
                    val injected = injectedValues != null
                    r = r || injected
                    if (!injected) return true
                    var i0 = i + 1
                    injectedValues.forEachFast { injectedValue ->
                        val delegatedConfig = config.delegatedWith(injectedValue).also { it.withParentConfig(containerConfig) }
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
