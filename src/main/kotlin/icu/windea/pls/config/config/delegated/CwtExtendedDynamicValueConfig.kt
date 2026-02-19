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
 * 动态值的扩展规则。
 *
 * 用于为对应的动态值提供额外的提示信息（如文档注释、内嵌提示）。
 *
 * 说明：
 * - 规则名称可以是常量、模板表达式、ANT 表达式或正则（见 [CwtDataTypeSets.PatternAware]）。
 *
 * 路径定位：`dynamic_values/{type}/{name}`，`{type}` 匹配动态值类型，`{name}` 匹配规则名称。
 *
 * CWTools 兼容性：PLS 扩展。
 *
 * 示例：
 * ```cwt
 * dynamic_values = {
 *     event_target = {
 *         ### Some documentation
 *         ## hint = §RSome hint text§!
 *         x # or `x = xxx`
 *     }
 * }
 * ```
 *
 * @property name 名称。
 * @property type 动态值类型。
 * @property hint 提示文本（可选）。
 */
interface CwtExtendedDynamicValueConfig : CwtDelegatedConfig<CwtMember, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    val type: String
    @FromOption("hint: string?")
    val hint: String?

    interface Resolver {
        /** 由成员规则解析为动态值的扩展规则。 */
        fun resolve(config: CwtMemberConfig<*>, type: String): CwtExtendedDynamicValueConfig
    }

    companion object : Resolver by CwtExtendedDynamicValueConfigResolverImpl()
}

// region Implementations

private class CwtExtendedDynamicValueConfigResolverImpl : CwtExtendedDynamicValueConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtMemberConfig<*>, type: String): CwtExtendedDynamicValueConfig = doResolve(config, type)

    private fun doResolve(config: CwtMemberConfig<*>, type: String): CwtExtendedDynamicValueConfig {
        val name = if (config is CwtPropertyConfig) config.key else config.value
        val hint = config.optionData.hint
        logger.debug { "Resolved extended dynamic value config (name: $name, type: $type).".withLocationPrefix(config) }
        return CwtExtendedDynamicValueConfigImpl(config, name, type, hint)
    }
}

private class CwtExtendedDynamicValueConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val type: String,
    override val hint: String?
) : UserDataHolderBase(), CwtExtendedDynamicValueConfig {
    override fun toString() = "CwtExtendedDynamicValueConfigImpl(name='$name', type='$type')"
}

// endregion
