package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.optimized
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.lang.util.ParadoxScopeManager

/**
 * 本地化提升规则。
 *
 * 用于提供本地化提升（localisation promotion）的相关信息（快速文档），并为其指定允许的作用域类型。
 *
 * **本地化命令提升（localisation command promotion）** 意指本地化命令字段的作用域提升逻辑，
 * 通过本地化链接切换作用域后，也可使用提升后的作用域匹配的本地化命令字段。
 * 可参见：`localizations.log`。
 *
 * 路径定位：`localisation_promotions/{name}`，`{name}` 匹配规则名称。
 *
 * CWTools 兼容性：兼容。
 *
 * 示例：
 * ```cwt
 * localisation_promotions = {
 *     Ruler = { country }
 *     # ...
 * }
 *
 * localisation_links = {
 *     ruler = { ... }
 *     # ...
 * }
 *
 * localisation_commands = {
 *     GetCountryType = { country }
 *     # ...
 * }
 *
 * # then `[Ruler.GetCountryType]` is valid after scope promotion
 * ```
 *
 * @property name 名称（匹配本地化链接名，忽略大小写）。
 * @property supportedScopes （提升后）允许的作用域（类型）的集合。
 *
 * @see CwtLocalisationCommandConfig
 * @see ParadoxCommandExpression
 * @see CwtDataTypes.DatabaseObject
 */
interface CwtLocalisationPromotionConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: @CaseInsensitive String
    @FromOption(": string | string[]")
    val supportedScopes: Set<String>

    interface Resolver {
        /** 由属性规则解析为本地化提升规则。 */
        fun resolve(config: CwtPropertyConfig): CwtLocalisationPromotionConfig
    }

    companion object : Resolver by CwtLocalisationPromotionConfigResolverImpl()
}

// region Implementations

private class CwtLocalisationPromotionConfigResolverImpl : CwtLocalisationPromotionConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtLocalisationPromotionConfig = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtLocalisationPromotionConfig {
        val name = config.key
        val supportedScopes = buildSet {
            config.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) }
            config.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) } }
        }.optimized()
        logger.debug { "Resolved localisation promotion config (name: $name).".withLocationPrefix(config) }
        return CwtLocalisationPromotionConfigImpl(config, name, supportedScopes)
    }
}

private class CwtLocalisationPromotionConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val supportedScopes: Set<String>
) : UserDataHolderBase(), CwtLocalisationPromotionConfig {
    override fun toString() = "CwtLocalisationPromotionConfigImpl(name='$name')"
}

// endregion
