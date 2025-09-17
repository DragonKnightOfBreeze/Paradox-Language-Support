package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtLocalisationCommandConfigResolverImpl
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.expression.ParadoxCommandExpression

/**
 * 本地化命令规则。
 *
 * 用于提供本地化命令字段（localisation command field）的相关信息（快速文档），并为其指定允许的作用域类型。
 *
 * **本地化命令字段（localisation command field）** 可在本地化文本中的命令表达式中使用，用于获取动态文本。
 * 其允许的作用域类型是预定义且兼容提升的。
 * 可参见：`localisations.log`。
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
 */
interface CwtLocalisationCommandConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: @CaseInsensitive String
    @FromOption(": string | string[]")
    val supportedScopes: Set<String>

    interface Resolver {
        /** 由属性规则解析为本地化命令规则。*/
        fun resolve(config: CwtPropertyConfig): CwtLocalisationCommandConfig
    }

    companion object : Resolver by CwtLocalisationCommandConfigResolverImpl()
}
