package icu.windea.pls.config.configExpression

/**
 * CWT 规则表达式的统一抽象。
 *
 * 在 CWT 规则与相关派生能力中，很多“字符串字段”需要被解析为结构化的表达式，
 * 例如：数据类型表达式、模板表达式、基数（cardinality）表达式、定位表达式等。
 * 这些解析结果会承载后续的导航、高亮、校验与补全等能力所需的元数据。
 *
 * @property expressionString 原始的表达式字符串（作为缓存键与相等性依据）。
 */
interface CwtConfigExpression {
    val expressionString: String
}
