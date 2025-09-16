package icu.windea.pls.config.config

import icu.windea.pls.config.config.impl.CwtOptionValueConfigResolverImpl
import icu.windea.pls.cwt.psi.CwtOption
import icu.windea.pls.model.CwtType

/**
 * 值选项（value option）对应的 CWT config。
 *
 * 概述：
 * - 对应成员 config（[CwtMemberConfig]）下以“无键”的形式声明的选项项，如：`## required`、`## primary`。
 * - 与 [CwtOptionConfig] 相对，后者是具有键的选项（`## key = value`）。
 * - 常用于为成员补充布尔标记或较短的语义标签。
 */
interface CwtOptionValueConfig : CwtOptionMemberConfig<CwtOption> {
    interface Resolver {
        /**
         * 从原始文本与上下文信息解析生成 [CwtOptionValueConfig]。
         *
         * 参数：
         * - [value]：选项值原文（例如 `required`）。
         * - [valueType]：选项值类型，默认按字符串处理。
         * - [optionConfigs]：下级选项集合，默认为空。
         */
        fun resolve(
            value: String,
            valueType: CwtType = CwtType.String,
            optionConfigs: List<CwtOptionMemberConfig<*>>? = null,
        ): CwtOptionValueConfig
    }

    companion object : Resolver by CwtOptionValueConfigResolverImpl()
}
