package icu.windea.pls.config.cwt.expression

/**
 * @property type 表达式类型，即CWT规则的dataType。
 * @property priority 优先级，需要依赖索引进行精确匹配的类型应当拥有更低的优先级。
 */
interface CwtKvExpression : CwtExpression {
	val type: CwtDataType
	val priority: Int
	val value: String?
	val extraValue: Any?
}

/**
 * 是否可能会使用到IDE索引。
 */
val CwtKvExpression.useIndex 
	get() = type == 