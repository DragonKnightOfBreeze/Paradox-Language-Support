package icu.windea.pls.ep.config.config

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.forEachReversedIndexedFast

/**
 * 用于在规则表达式级别注入规则。
 *
 * @see CwtInjectedConfigProcessor
 */
class CwtConfigExpressionBasedInjectedConfigProvider : CwtInjectedConfigProvider {
    @Optimized
    override fun injectConfigs(parentConfig: CwtMemberConfig<*>, containerConfig: CwtMemberConfig<*>, configs: MutableList<CwtMemberConfig<*>>): Boolean {
        // NOTE 3.0.1 optimize: 来自 gemini-3.1-pro：将密集的类型检查改为访问者模式，性能通常会更差，而不是更好
        //  - 现代 JVM 对 `instanceof` 指令做了极度深度的优化，比如内联缓存和分支预测；而访问者模式需要两次虚方法调用，这会查找虚方法表（vtable/itable），且会增加方法栈帧的压栈和出栈开销。
        //  - 总之这里的类型检查不是非常明显的性能热点，时间复杂度仍然是无法避免的，让事情变得简单一点。
        var r = false
        val processors = CwtInjectedConfigProcessor.EP_NAME.extensionList
        configs.forEachReversedIndexedFast { i, config ->
            when (config) {
                is CwtPropertyConfig -> {
                    val keyExpression = config.keyExpression // 3.0.1 optimize: access expressionString only on demand
                    val valueExpression = config.valueExpression // 3.0.1 optimize: access expressionString only on demand
                    processors.forEachFast f@{ processor ->
                        if (!processor.supports(parentConfig)) return@f
                        val injectedKeys = processor.processKey(parentConfig, config, keyExpression)
                        val injectedValues = processor.processValue(parentConfig, config, valueExpression)
                        val keyInjected = injectedKeys != null
                        val valueInjected = injectedValues != null
                        val injected = keyInjected || valueInjected
                        r = r || injected
                        if (!injected) return true
                        var i0 = i + 1
                        if (keyInjected) {
                            injectedKeys.forEachFast { injectedKey ->
                                if (valueInjected) {
                                    injectedValues.forEachFast { injectedValue ->
                                        val delegatedConfig = config.delegatedWith(injectedKey, injectedValue).also { it.withParentConfig(containerConfig) }
                                        configs.add(i0, delegatedConfig)
                                        i0++
                                    }
                                } else {
                                    val injectedValue = valueExpression.expressionString
                                    val delegatedConfig = config.delegatedWith(injectedKey, injectedValue).also { it.withParentConfig(containerConfig) }
                                    configs.add(i0, delegatedConfig)
                                    i0++
                                }
                            }
                        } else {
                            val injectedKey = keyExpression.expressionString
                            injectedValues?.forEachFast { injectedValue ->
                                val delegatedConfig = config.delegatedWith(injectedKey, injectedValue).also { it.withParentConfig(containerConfig) }
                                configs.add(i0, delegatedConfig)
                                i0++
                            }
                        }
                        if (!processor.keepOrigin(config)) configs.removeAt(i)
                    }
                }
                is CwtValueConfig -> {
                    val valueExpression = config.valueExpression // 3.0.1 optimize: access expressionString only on demand
                    processors.forEachFast f@{ processor ->
                        if (!processor.supports(parentConfig)) return@f
                        val injectedValues = processor.processValue(parentConfig, config, valueExpression)
                        val injected = injectedValues != null
                        r = r || injected
                        if (!injected) return true
                        var i0 = i + 1
                        injectedValues.forEachFast { injectedValue ->
                            val delegatedConfig = config.delegatedWith(injectedValue).also { it.withParentConfig(containerConfig) }
                            configs.add(i0, delegatedConfig)
                            i0++
                        }
                        if (!processor.keepOrigin(config)) configs.removeAt(i)
                    }
                }
            }
        }
        return r
    }
}
