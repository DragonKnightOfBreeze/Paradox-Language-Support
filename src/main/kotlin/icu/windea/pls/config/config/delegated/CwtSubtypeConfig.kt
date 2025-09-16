package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtSubtypeConfigResolverImpl
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.util.ReversibleValue
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 子类型规则（subtype[...]）。
 *
 * 概述：
 * - 从类型规则（[CwtTypeConfig]）中拆分出的细分分组，用于限定“类型键”的匹配范围与命名约束。
 * - 常用于过滤特定前缀、排除名单等，以优化补全与校验体验。
 *
 * @property name 子类型名（来自 `subtype[$]`）。
 * @property typeKeyFilter 类型键过滤器（包含/排除，大小写不敏感）。
 * @property typeKeyRegex 类型键正则过滤器（忽略大小写）。
 * @property startsWith 类型键前缀要求（大小写敏感与否取决于实现，这里按字面匹配）。
 * @property onlyIfNot 排除名单：名称不在集合内才匹配。
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

    /** 判断该子类型是否属于名为 [groupName] 的分组（若声明了分组信息）。*/
    fun inGroup(groupName: String): Boolean

    interface Resolver {
        /** 由 `subtype[...]` 的属性规则解析为子类型规则。*/
        fun resolve(config: CwtPropertyConfig): CwtSubtypeConfig?
    }

    companion object : Resolver by CwtSubtypeConfigResolverImpl()
}
