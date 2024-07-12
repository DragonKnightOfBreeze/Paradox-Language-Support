package icu.windea.pls.model.expression

import icu.windea.pls.model.*

/**
 * 定义类型表达式。
 *
 * 示例：
 *
 * * `event`
 * * `event.hidden`
 * * `event.hidden.country_event`
 *
 * 用途：
 *
 * * 查询定义时指定定义类型表达式，以进行过滤。
 * * 在CWT规则文件中，`<X>`表示一个定义引用，其中`X`即是一个定义类型表达式。
 */
class ParadoxDefinitionTypeExpression private constructor(
    val expressionString: String
) {
    val type: String
    val subtypes: List<String>
    
    init {
        val dotIndex = expressionString.indexOf('.')
        type = if(dotIndex == -1) expressionString else expressionString.substring(0, dotIndex)
        subtypes = if(dotIndex == -1) emptyList() else expressionString.substring(dotIndex + 1).split('.')
    }
    
    operator fun component1() = type
    
    operator fun component2() = subtypes
    
    fun matches(type: String, subtypes: Collection<String>): Boolean {
        return type == this.type && subtypes.containsAll(this.subtypes)
    }
    
    fun matches(typeExpression: ParadoxDefinitionTypeExpression): Boolean {
        return matches(typeExpression.type, typeExpression.subtypes)
    }
    
    fun matches(typeExpression: String): Boolean {
        return matches(resolve(typeExpression))
    }
    
    fun matches(definitionInfo: ParadoxDefinitionInfo): Boolean {
        return matches(definitionInfo.type, definitionInfo.subtypes)
    }
    
    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxDefinitionTypeExpression && expressionString == other.expressionString
    }
    
    override fun hashCode(): Int {
        return expressionString.hashCode()
    }
    
    override fun toString(): String {
        return expressionString
    }
    
    companion object Resolver {
        fun resolve(expressionString: String): ParadoxDefinitionTypeExpression {
            return ParadoxDefinitionTypeExpression(expressionString)
        }
    }
}
