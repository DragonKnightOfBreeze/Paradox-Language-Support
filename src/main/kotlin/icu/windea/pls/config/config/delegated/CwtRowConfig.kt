package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.impl.CwtRowConfigResolverImpl

/**
 * 行规则。
 *
 * 用于描述 CSV 文件中每一行允许的列的列名与可选值，从而在 CSV 文件中提供代码补全、代码检查等功能。
 * 按照路径模式匹配 CSV 文件。
 *
 * 路径定位：`rows/row[{name}]`，`{name}` 匹配规则名称（行名）。
 *
 * CWTools 兼容性：PLS 扩展。
 *
 * 示例：
 * ```cwt
 * rows = {
 *     row[component_template] = {
 *         path = "game/common/component_templates"
 *         file_extension = .csv
 *         columns = {
 *             key = <component_template>
 *             # ...
 *         }
 *     }
 * }
 * ```
 *
 * @property name 名称（行名）。
 * @property columns 各列名到对应列规则的映射。
 * @property endColumn 若匹配到该列名，视作可省略的最后一列。
 */
interface CwtRowConfig : CwtFilePathMatchableConfig {
    @FromKey("row[$]")
    val name: String
    @FromProperty("columns: ColumnConfigs")
    val columns: Map<String, CwtPropertyConfig>
    @FromProperty("end_column: string?")
    val endColumn: String?

    interface Resolver {
        /** 由属性规则解析为行规则。*/
        fun resolve(config: CwtPropertyConfig): CwtRowConfig?
    }

    companion object : Resolver by CwtRowConfigResolverImpl()
}
