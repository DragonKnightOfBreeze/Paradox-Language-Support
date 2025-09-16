package icu.windea.pls.config.config

import icu.windea.pls.config.config.impl.CwtOptionConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtOption
import icu.windea.pls.model.CwtSeparatorType
import icu.windea.pls.model.CwtType

/**
 * 键值选项（option）对应的 CWT config。
 *
 * 概述：
 * - 对应成员 config（[CwtMemberConfig]）下以 `## key = value` 形式声明的选项项。
 * - 与 [CwtOptionValueConfig] 相对，后者是“无键的值选项”（如 `## required`）。
 *
 * 字段：
 * - [key]：选项键。
 * - [separatorType]：分隔符类型（`=`/`:` 等），用于保留书写信息并在必要时影响解析。
 *
 * 参考：
 * - references/cwt/guidance.md（选项写法）
 * - docs/zh/config.md（选项扩展与消费）
 */
interface CwtOptionConfig : CwtOptionMemberConfig<CwtOption> {
    /** 选项键。 */
    val key: String
    /** 分隔符类型（`=`/`:` 等）。 */
    val separatorType: CwtSeparatorType

    interface Resolver {
        /**
         * 从原始文本与上下文信息解析生成 [CwtOptionConfig]。
         *
         * 参数：
         * - [key]：选项键。
         * - [value]：选项值原文。
         * - [valueType]：选项值类型，默认按字符串处理。
         * - [separatorType]：分隔符类型，默认 `=`。
         * - [optionConfigs]：下级选项集合，默认为空。
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
