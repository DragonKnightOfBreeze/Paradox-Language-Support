package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.impl.CwtEnumConfigResolverImpl
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 枚举规则：`enum[<name>] = { values = [ ... ] }` 或 `enum[<name>] = [ ... ]`。
 *
 * - 表示一个静态可枚举的字符串集合，常用于参数取值、配置项限制等。
 * - `values` 接受模板字符串（template_expression），在使用处可进行模板替换。
 * - PLS 会为每个值生成对应的 [CwtValueConfig] 并建立 [valueConfigMap] 以支持跳转/重命名/补全。
 * - 值大小写不敏感（[CaseInsensitive]）。
 */
interface CwtEnumConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("enum[$]")
    val name: String
    @FromProperty("values: template_expression[]")
    val values: Set<@CaseInsensitive String>

    val valueConfigMap: Map<@CaseInsensitive String, CwtValueConfig>

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtEnumConfig?
    }

    companion object : Resolver by CwtEnumConfigResolverImpl()
}
