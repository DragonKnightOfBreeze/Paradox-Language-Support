package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.values.ReversibleValue
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 子类型规则。
 *
 * 用于描述如何匹配定义的子类型，从而提供更准确的代码补全、代码检查等功能。
 *
 * 路径定位：`types/type[{type}]/subtype[{subtype}]`，`{type}` 匹配定义类型， `{subtype}` 匹配定义子类型。
 *
 * CWTools 兼容性：兼容，但存在一些扩展。
 *
 * 示例：
 * ```cwt
 * types = {
 *     type[civic_or_origin] = {
 *         path = "game/common/governments/civics"
 *         path_extension = .txt
 *         subtype[origin] = {
 *             is_origin = yes
 *             # ...
 *         }
 *         # ...
 *     }
 * }
 * ```
 *
 * @property name 子类型名。
 * @property typeKeyFilter 类型键过滤器（包含/排除，忽略大小写）。
 * @property typeKeyRegex 类型键正则过滤器（忽略大小写）。
 * @property startsWith 类型键前缀要求（大小写敏感与否取决于实现，这里按字面匹配）。
 * @property onlyIfNot 排除名单：名称不在集合内才匹配。
 * @property group 分组名。
 *
 * @see CwtTypeConfig
 */
interface CwtSubtypeConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("subtype[$]")
    val name: String
    @FromOption("type_key_filter: string | string[]")
    val typeKeyFilter: ReversibleValue<Set<@CaseInsensitive String>>?
    @FromOption("type_key_regex: string?")
    val typeKeyRegex: Regex?
    @FromOption("starts_with: string?")
    val startsWith: String?
    @FromOption("only_if_not: string[]?")
    val onlyIfNot: Set<String>?
    @FromOption("group: string?")
    val group: String?

    interface Resolver {
        /** 由属性规则解析为子类型规则。 */
        fun resolve(config: CwtPropertyConfig): CwtSubtypeConfig?
    }

    companion object : Resolver by CwtSubtypeConfigResolverImpl()
}

// region Implementations

private class CwtSubtypeConfigResolverImpl : CwtSubtypeConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtSubtypeConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtSubtypeConfig? {
        val name = config.key.removeSurroundingOrNull("subtype[", "]")?.orNull()?.optimized() ?: return null
        val typeKeyFilter = config.optionData.typeKeyFilter
        val typeKeyRegex = config.optionData.typeKeyRegex
        val startsWith = config.optionData.startsWith
        val onlyIfNot = config.optionData.onlyIfNot
        val group = config.optionData.group
        logger.debug { "Resolved subtype config (name: $name).".withLocationPrefix(config) }
        return CwtSubtypeConfigImpl(config, name, typeKeyFilter, typeKeyRegex, startsWith, onlyIfNot, group)
    }
}

private class CwtSubtypeConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val typeKeyFilter: ReversibleValue<Set<String>>? = null,
    override val typeKeyRegex: Regex? = null,
    override val startsWith: String? = null,
    override val onlyIfNot: Set<String>? = null,
    override val group: String? = null,
) : UserDataHolderBase(), CwtSubtypeConfig {
    override fun toString() = "CwtSubtypeConfigImpl(name='$name')"
}

// endregion
