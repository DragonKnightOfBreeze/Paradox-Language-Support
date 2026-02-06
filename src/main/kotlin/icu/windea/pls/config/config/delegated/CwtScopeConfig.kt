package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.collections.caseInsensitiveStringSet
import icu.windea.pls.core.collections.getOne
import icu.windea.pls.core.optimized
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.model.scope.ParadoxScope
import icu.windea.pls.model.scope.ParadoxScopeContext

/**
 * 作用域规则。
 *
 * 用于提供作用域类型（scope type）的相关信息（快速文档、别名）。
 *
 * 路径定位：`scopes/{name}`，`{name}` 匹配规则名称。
 *
 * CWTools 兼容性：兼容。
 *
 * 示例：
 * ```cwt
 * scopes = {
 *     Country = { aliases = { country } }
 * }
 * ```
 *
 * @property name 规则名称。
 * @property aliases 该作用域的别名集合（忽略大小写）。
 * @property isSubscopeOf TODO 暂不支持。
 *
 * @see ParadoxScope
 * @see ParadoxScopeContext
 */
interface CwtScopeConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: String
    @FromProperty("aliases: string[]")
    val aliases: Set<@CaseInsensitive String>
    @FromProperty("is_subscope_of: string?")
    val isSubscopeOf: String?

    interface Resolver {
        /** 由属性规则解析为作用域规则。 */
        fun resolve(config: CwtPropertyConfig): CwtScopeConfig?
    }

    companion object : Resolver by CwtScopeConfigResolverImpl()
}

// region Implementations

private class CwtScopeConfigResolverImpl : CwtScopeConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtScopeConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtScopeConfig? {
        val name = config.key
        val propElements = config.properties
        if (propElements == null) {
            logger.warn("Skipped invalid scope config (name: $name): Null properties.".withLocationPrefix(config))
            return null
        }
        val propGroup = propElements.groupBy { it.key }
        val aliases = propGroup.getOne("aliases")?.let { prop ->
            prop.values?.mapNotNullTo(caseInsensitiveStringSet()) { it.stringValue }
        }?.optimized().orEmpty()
        val isSubscopeOf = propGroup.getOne("is_subscope_of")?.stringValue
        logger.debug { "Resolved scope config (name: $name).".withLocationPrefix(config) }
        return CwtScopeConfigImpl(config, name, aliases, isSubscopeOf)
    }
}

private class CwtScopeConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val aliases: Set<String>,
    override val isSubscopeOf: String?,
) : UserDataHolderBase(), CwtScopeConfig {
    override fun toString() = "CwtScopeConfigImpl(name='$name')"
}

// endregion
