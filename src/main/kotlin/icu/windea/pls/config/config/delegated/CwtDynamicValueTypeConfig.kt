package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.impl.CwtDynamicValueTypeConfigResolverImpl
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 动态值类型规则（value[...]）。
 *
 * 概述：
 * - 声明一组“动态值名称”的集合，通常与具体的值解析器/引用机制配合，提供补全与校验。
 * - 由 `value[name] = { values = [...] }` 声明。
 *
 * @property name 名称（来自 `value[$]`）。
 * @property values 可选项集合（模板表达式，大小写不敏感比对）。
 *
 * 计算字段：
 * @property valueConfigMap 可选项到其原始值规则的映射。
 */
interface CwtDynamicValueTypeConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("value[$]")
    val name: String
    @FromProperty("values: template_expression[]")
    val values: Set<@CaseInsensitive String>

    val valueConfigMap: Map<@CaseInsensitive String, CwtValueConfig>

    interface Resolver {
        /** 由 `value[...]` 的属性规则解析为动态值类型规则。*/
        fun resolve(config: CwtPropertyConfig): CwtDynamicValueTypeConfig?
    }

    companion object : Resolver by CwtDynamicValueTypeConfigResolverImpl()
}
