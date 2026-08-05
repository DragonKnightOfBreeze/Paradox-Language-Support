package icu.windea.pls.ep.config.config

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.model.ParadoxGameType

/**
 * 用于在规则表达式级别注入规则时，处理规则表达式。同时标记是否进行了注入，以及是否要保留原始规则。
 *
 * @see CwtInjectedConfigProvider
 * @see CwtConfigExpressionBasedInjectedConfigProvider
 */
interface CwtInjectedConfigProcessor {
    fun supports(gameType: ParadoxGameType): Boolean = true

    fun supports(parentConfig: CwtMemberConfig<*>): Boolean = true

    /**
     * 对键或值进行注入，返回得到的表达式字符串列表。如果为 `null`，则表示不进行注入。
     *
     * @param parentConfig 原始的父规则。
     * @param config 当前正在遍历的规则。
     * @param configExpression 当前正在遍历的数据表达式。
     */
    fun process(parentConfig: CwtMemberConfig<*>, config: CwtMemberConfig<*>, configExpression: CwtDataExpression): List<String>?

    /**
     * 对键进行注入，返回得到的表达式字符串列表。如果为 `null`，则表示不进行注入。
     *
     * @param parentConfig 原始的父规则。
     * @param config 当前正在遍历的规则。
     * @param configExpression 当前正在遍历的数据表达式。
     */
    fun processKey(parentConfig: CwtMemberConfig<*>, config: CwtMemberConfig<*>, configExpression: CwtDataExpression): List<String>? = process(parentConfig, config, configExpression)

    /**
     * 对值进行注入，返回得到的表达式字符串列表。如果为 `null`，则表示不进行注入。
     *
     * @param parentConfig 原始的父规则。
     * @param config 当前正在遍历的规则。
     * @param configExpression 当前正在遍历的数据表达式。
     */
    fun processValue(parentConfig: CwtMemberConfig<*>, config: CwtMemberConfig<*>, configExpression: CwtDataExpression): List<String>? = process(parentConfig, config, configExpression)

    /**
     * 是否需要保留原始规则。
     */
    fun keepOrigin(config: CwtMemberConfig<*>) = true

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<CwtInjectedConfigProcessor>("icu.windea.pls.injectedConfigProcessor")
    }
}
