package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtRowConfigResolverImpl

/**
 * 行规则（row[...]）。
 *
 * 概述：
 * - 声明一行由若干“列”组成的结构，并指明列的规则与末列省略规则。
 * - 由 `row[name] = { columns = {...} }` 声明。
 *
 * @property name 行名（来自 `row[$]`）。
 * @property columns 各列名到对应列规则的映射。
 * @property endColumn 若匹配到该列名，视作最后一列，可在之后省略。
 */
interface CwtRowConfig : CwtFilePathMatchableConfig {
    @FromKey("row[$]")
    val name: String
    @FromProperty("columns: ColumnConfigs")
    val columns: Map<String, CwtPropertyConfig>
    @FromProperty("end_column: string?")
    val endColumn: String?

    interface Resolver {
        /** 由 `row[...]` 的属性规则解析为行规则。*/
        fun resolve(config: CwtPropertyConfig): CwtRowConfig?
    }

    companion object : Resolver by CwtRowConfigResolverImpl()
}
