package icu.windea.pls.config.configExpression

import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.core.util.FloatRangeInfo
import icu.windea.pls.core.util.IntRangeInfo

/**
 * 数据表达式的元数据。
 *
 * 说明：
 * - 绝大部分字符串类型的元数据都可以通过 [value] 获取。
 * - 对于常量类型（[Constant][CwtDataTypes.Constant]）和模板类型（[Template][CwtDataTypes.Template]）的数据表达式，需要直接通过 `expressionString` 获取表达式字符串，而非通过 [value] 获取。
 *
 * 参考：
 * - 规则系统的说明文档：[config.md](https://windea.icu/Paradox-Language-Support/config.md)
 * - 规则格式的参考手册：[ref-config-format.md](https://windea.icu/Paradox-Language-Support/ref-config-format.md)
 *
 * @see CwtDataExpression
 */
interface CwtDataExpressionMetadata {
    val value: String? get() = null
    val wildcard: Boolean get() = false
    val condition: Boolean get() = false
    val ignoreCase: Boolean get() = false
    val intRange: IntRangeInfo? get() = null
    val floatRange: FloatRangeInfo? get() = null
    val suffixes: Set<String>? get() = null
}
