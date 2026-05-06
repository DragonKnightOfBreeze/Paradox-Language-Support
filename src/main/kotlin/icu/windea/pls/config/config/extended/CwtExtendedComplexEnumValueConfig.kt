package icu.windea.pls.config.config.extended

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.annotations.FromName
import icu.windea.pls.config.annotations.FromOptionMember
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtIdMatchableConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.cwt.psi.CwtMember

/**
 * 复杂枚举值的扩展规则。
 *
 * 用于为对应的复杂枚举值提供额外的提示信息（文档注释和内嵌提示）。
 *
 * 说明：
 * - 规则名称可以是常量、模板表达式、ANT 表达式或正则（见 [CwtDataTypeSets.PatternAware]）。
 *
 * 路径定位：`complex_enum_values/{type}/{name}`，`{type}` 匹配枚举名，`{name}` 匹配规则名称。
 *
 * CWTools 兼容性：不兼容。插件作为扩展提供。
 *
 * 示例：
 * ```cwt
 * complex_enum_values = {
 *     component_tag = {
 *         ### Some documentation
 *         ## hint = §RSome hint text§!
 *         x # or `x = xxx`
 *     }
 * }
 * ```
 *
 * @property name 名称。
 * @property type 枚举名。
 * @property hint 提示文本（可选）。
 */
interface CwtExtendedComplexEnumValueConfig : CwtDelegatedConfig<CwtMember, CwtMemberConfig<*>>, CwtIdMatchableConfig<CwtMember> {
    @FromName
    val name: String
    val type: String
    @FromOptionMember("hint: string?")
    val hint: String?

    interface Resolver {
        /** 由成员规则解析为复杂枚举值的扩展规则。 */
        fun resolve(config: CwtMemberConfig<*>, type: String): CwtExtendedComplexEnumValueConfig
    }

    companion object : Resolver by CwtExtendedComplexEnumValueConfigResolverImpl()
}

// region Implementations

private class CwtExtendedComplexEnumValueConfigResolverImpl : CwtExtendedComplexEnumValueConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtMemberConfig<*>, type: String): CwtExtendedComplexEnumValueConfig = doResolve(config, type)

    private fun doResolve(config: CwtMemberConfig<*>, type: String): CwtExtendedComplexEnumValueConfig {
        val name = if (config is CwtPropertyConfig) config.key else config.value
        val hint = config.optionData.hint
        logger.debug { "Resolved extended complex enum value config (name: $name, type: $type).".withLocationPrefix(config) }
        return CwtExtendedComplexEnumValueConfigImpl(config, name, type, hint)
    }
}

private class CwtExtendedComplexEnumValueConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val type: String,
    override val hint: String?
) : UserDataHolderBase(), CwtExtendedComplexEnumValueConfig {
    override fun toString() = "CwtExtendedComplexEnumValueConfigImpl(name='$name', type='$type')"
}

// endregion
