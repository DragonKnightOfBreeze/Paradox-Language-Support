package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.cwt.psi.CwtMember

/**
 * on action 的扩展规则。
 *
 * 用于为对应的 on action 提供额外的提示信息（如文档注释、内嵌提示），以及指定事件类型。
 *
 * 说明：
 * - 规则名称可以是常量、模板表达式、ANT 表达式或正则（见 [CwtDataTypeSets.PatternAware]）。
 * - on action 即类型为 `on_action` 的定义。
 * - 事件类型是通过 `## event_type` 选项指定的。这会重载声明规则中的所有对事件的引用为对该类型事件的引用。
 *
 * 路径定位：`on_actions/{name}`，`{name}` 匹配规则名称。
 *
 * CWTools 兼容性：不兼容，拥有不同的格式与行为。
 *
 * 示例：
 * ```cwt
 * on_actions = {
 *     ### Some documentation
 *     ## hint = §RSome hint text§!
 *     ## replace_scopes = { this = country root = country }
 *     ## event_type = country
 *     x
 * }
 * ```
 *
 * @property name 名称。
 * @property eventType 事件类型。
 * @property hint 额外提示信息（可选）。
 */
interface CwtExtendedOnActionConfig : CwtDelegatedConfig<CwtMember, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    @FromOption("event_type: string")
    val eventType: String
    @FromOption("hint: string?")
    val hint: String?

    interface Resolver {
        /** 由成员规则解析为 on action 的扩展规则。 */
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedOnActionConfig?
    }

    companion object : Resolver by CwtExtendedOnActionConfigResolverImpl()
}

// region Implementations

private class CwtExtendedOnActionConfigResolverImpl : CwtExtendedOnActionConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtMemberConfig<*>): CwtExtendedOnActionConfig? = doResolve(config)

    private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedOnActionConfig? {
        val name = if (config is CwtPropertyConfig) config.key else config.value
        val eventType = config.optionData.eventType
        if (eventType == null) {
            logger.warn("Skipped invalid extended on action config (name: $name): Missing event_type option.".withLocationPrefix(config))
            return null
        }
        val hint = config.optionData.hint
        logger.debug { "Resolved extended on action config (name: $name, event type: $eventType).".withLocationPrefix(config) }
        return CwtExtendedOnActionConfigImpl(config, name, eventType, hint)
    }
}

private class CwtExtendedOnActionConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val eventType: String,
    override val hint: String?
) : UserDataHolderBase(), CwtExtendedOnActionConfig {
    override fun toString() = "CwtExtendedOnActionConfigImpl(name='$name', eventType='$eventType')"
}

// endregion
