package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.impl.CwtEnumConfigResolverImpl
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 简单枚举规则（enum[...]）。
 *
 * 概述：
 * - 声明一组“固定可选项”，用于限定值域、提供补全与校验。
 * - 由 `enum[name] = { values = [...] }` 声明。
 *
 * 定位与命名：`enums/enum[\$]`，`$` 匹配规则名称。
 *
 * 示例：
 * ```cwt
 * # stellaris:enums.cwt
 * enums = {
 *     enum[weight_or_base] = { weight base }
 * }
 * ```
 *
 * @property name 名称。
 * @property values 可选项集合（模板表达式，大小写不敏感比对）。
 * @property valueConfigMap （计算属性）可选项到其原始值规则的映射。
 */
interface CwtEnumConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("enum[$]")
    val name: String
    @FromProperty("values: template_expression[]")
    val values: Set<@CaseInsensitive String>

    val valueConfigMap: Map<@CaseInsensitive String, CwtValueConfig>

    interface Resolver {
        /** 由 `enum[...]` 的属性规则解析为简单枚举规则。*/
        fun resolve(config: CwtPropertyConfig): CwtEnumConfig?
    }

    companion object : Resolver by CwtEnumConfigResolverImpl()
}
