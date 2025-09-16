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
 * 定位：
 * - 在 `FileBasedCwtConfigGroupDataProvider.processFile` 中，读取顶层键 `rows` 下的每个成员属性。
 * - 行名从成员属性键中提取：去除前后缀 `row[` 与 `]`，得到 `name`。
 *
 * 例：
 * ```cwt
 * # 示例（来自 core/internal/schema.cwt 模板）
 * rows = {
 *     row[csv_line] = {
 *         columns = { id = scalar name = scalar }
 *         end_column = name
 *     }
 * }
 * ```
 *
 * @property name 行名。
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
