package icu.windea.pls.config.configExpression

/**
 * CWT规则表达式。
 *
 * 在CWT规则中，特定的字符串需要被解析为表达式，以获取需要的元数据。
 */
interface CwtConfigExpression {
    val expressionString: String
}
