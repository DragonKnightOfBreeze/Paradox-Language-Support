package icu.windea.pls.config.expression.internal

import com.google.common.cache.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.util.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*

sealed interface CwtSchemaExpression: CwtExpression {
    class Constant(
        override val expressionString: String
    ): CwtSchemaExpression
    
    /**
     * @property pattern 对应的ANT表达式。
     * @property parameterRanges 参数在表达式字符串中的文本范围的列表。
     */
    class Template(
        override val expressionString: String,
        val pattern: String,
        val parameterRanges: List<TextRange>
    ): CwtSchemaExpression
    
    /**
     * @property name 类型名。
     */
    class Type(
        override val expressionString: String,
        val name: String
    ): CwtSchemaExpression
    
    /**
     * @property name 枚举名。
     */
    class Enum(
        override val expressionString: String,
        val name: String
    ): CwtSchemaExpression
    
    companion object Resolver {
        private val cache = CacheBuilder.newBuilder().buildCache<String, CwtSchemaExpression> { doResolve(it) }
        private val parameterRegex = """(?<!\\)\$.*?\$""".toRegex()
        
        fun resolve(expressionString: String): CwtSchemaExpression = cache.get(expressionString)
        
        private fun doResolve(expressionString: String): CwtSchemaExpression {
            val indices = expressionString.indicesOf('$')
            if(indices.isEmpty()) {
                return Constant(expressionString)
            }
            if(indices.size == 1) {
                run { 
                    val name = expressionString.removePrefixOrNull("\$enum:") ?: return@run
                    return Enum(expressionString, name)
                }
                run {
                    val name = expressionString.removePrefixOrNull("\$") ?: return@run
                    return Type(expressionString, name)
                }
            }
            if(indices.size % 2 == 1) {
                thisLogger().warn("Invalid schema expression $expressionString, fallback to constant")
                return Constant(expressionString)
            }
            val pattern = expressionString.replace(parameterRegex, "*")
            val parameterRanges = indices.windowed(2) { (i1, i2) ->
                TextRange.create(i1, i2 + 1)
            }
            return Template(expressionString, pattern, parameterRanges)
        }
    }
}

