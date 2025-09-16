package icu.windea.pls.config.config

import icu.windea.pls.config.config.impl.CwtOptionConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtOption
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.CwtType

/**
 * 键值选项（option）对应的规则。
 *
 * 概述：
 * - 对应成员规则（[CwtMemberConfig]）下以 `## key = value` 形式声明的选项项。
 * - 与 [CwtOptionValueConfig] 相对，后者是“无键的值选项”（如 `## required`）。
 *
 * 参考：
 * - references/cwt/guidance.md（选项写法）
 * - docs/zh/config.md（选项扩展与消费）
 *
 * @property key 选项键。
 * @property separatorType 分隔符类型（`=`/`:` 等），用于保留书写信息并在必要时影响解析。
 */
interface CwtOptionConfig : CwtOptionMemberConfig<CwtOption> {
    val key: String
    val separatorType: CwtSeparatorType

    interface Resolver {
        /**
         * 从 [key]/[value] 等信息解析生成规则；[separatorType] 默认 `=`，可携带下级 [optionConfigs]。
         */
        fun resolve(
            key: String,
            value: String,
            valueType: CwtType = CwtType.String,
            separatorType: CwtSeparatorType = CwtSeparatorType.EQUAL,
            optionConfigs: List<CwtOptionMemberConfig<*>>? = null,
        ): CwtOptionConfig
    }

    companion object : Resolver by CwtOptionConfigResolverImpl()
}
