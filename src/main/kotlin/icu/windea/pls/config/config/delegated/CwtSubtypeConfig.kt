package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtSubtypeConfigResolverImpl
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.util.ReversibleValue
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 子类型规则：`subtype[<name>] = { ... }`。
 *
 * - 作为 `type[...]` 下的细分，用于进一步限定“类型键”的可接受范围，并影响补全/引用解析等。
 * - 可通过若干过滤条件与前缀匹配来确定哪些定义属于该子类型。
 *
 * 字段语义：
 * - `typeKeyFilter`: 允许或排除的类型键集合（忽略大小写），内部用“可反转集合”表达 `=`/`!=` 语义。
 * - `typeKeyRegex`: 用正则匹配类型键（忽略大小写）。
 * - `startsWith`: 以指定前缀开头的类型键。注意：区分大小写（与 `type[...]` 中对应字段不同）。
 * - `onlyIfNot`: 若存在这些键，则当前子类型不生效（排他条件）。
 *
 * 其它：
 * - `inGroup(groupName)`: 判断该子类型是否声明了选项 `group = <groupName>`，用于在 UI/校验中对同组子类型聚合展示。
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

    fun inGroup(groupName: String): Boolean

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtSubtypeConfig?
    }

    companion object : Resolver by CwtSubtypeConfigResolverImpl()
}
