package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtRowConfigResolverImpl

/**
 * 行规则：`row[<name>] = { columns = { ... } }`，用于 CSV/表格结构的列匹配与校验。
 *
 * - 可配合 `CwtFilePathMatchableConfig` 的路径字段限制生效范围。
 * - `columns` 定义每一列的 CWT 规则；按列名匹配。
 * - `endColumn` 若指定，当遇到该列名时，后续列可以省略。
 *
 * @property columns 各个列对应的 CWT 规则。
 * @property endColumn 如果匹配最后一列的列名，则该列可以省略。
 */
interface CwtRowConfig : CwtFilePathMatchableConfig {
    @FromKey("row[$]")
    val name: String
    @FromProperty("columns: ColumnConfigs")
    val columns: Map<String, CwtPropertyConfig>
    @FromProperty("end_column: string?")
    val endColumn: String?

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtRowConfig?
    }

    companion object : Resolver by CwtRowConfigResolverImpl()
}
