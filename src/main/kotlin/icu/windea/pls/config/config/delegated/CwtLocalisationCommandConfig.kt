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
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.optimized
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.resolve.complexExpression.ParadoxCommandExpression
import icu.windea.pls.model.scope.ParadoxScopeId

/**
 * 本地化命令规则。
 *
 * 用于提供本地化命令字段（localisation command field）的相关信息（快速文档），并为其指定允许的作用域类型。
 *
 * **本地化命令字段（localisation command field）** 可在本地化文本中的命令表达式中使用，用于获取动态文本。
 * 其允许的作用域类型是预定义且兼容提升的。
 * 可参见：`localizations.log`。
 *
 * 在语义与格式上，它们类似编程语言中的属性或字段。

 * 路径定位：`localisation_commands/{name}`，`{name}` 匹配规则名称（命令字段名称）。
 *
 * CWTools 兼容性：兼容。
 *
 * 示例：
 * ```cwt
 * localisation_commands = {
 *     GetCountryType = { country }
 *     # ...
 * }
 *
 * # then `[Owner.GetCountryType]` can be used in localisation text
 * ```
 *
 * @property name 名称（命令字段名称，忽略大小写）。
 * @property supportedScopes 允许的作用域（类型）的集合。
 *
 * @see CwtLocalisationPromotionConfig
 * @see ParadoxCommandExpression
 * @see CwtDataTypes.DatabaseObject
 */
interface CwtLocalisationCommandConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: @CaseInsensitive String
    @FromOption(": string | string[]")
    val supportedScopes: Set<String>

    interface Resolver {
        /** 由属性规则解析为本地化命令规则。 */
        fun resolve(config: CwtPropertyConfig): CwtLocalisationCommandConfig
    }

    companion object : Resolver by CwtLocalisationCommandConfigResolverImpl()
}

// region Implementations

private class CwtLocalisationCommandConfigResolverImpl : CwtLocalisationCommandConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtLocalisationCommandConfig = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtLocalisationCommandConfig {
        val name = config.key
        val supportedScopes = buildSet {
            config.stringValue?.let { v -> add(ParadoxScopeId.getId(v)) }
            config.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeId.getId(v)) } }
        }.optimized().orNull() ?: ParadoxScopeId.anyScopeIdSet
        logger.debug { "Resolved localisation command config (name: $name).".withLocationPrefix(config) }
        return CwtLocalisationCommandConfigImpl(config, name, supportedScopes)
    }
}

private class CwtLocalisationCommandConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val supportedScopes: Set<String>
) : UserDataHolderBase(), CwtLocalisationCommandConfig {
    override fun toString() = "CwtLocalisationCommandConfigImpl(name='$name')"
}

// endregion
