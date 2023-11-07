package icu.windea.pls.config.expression

import com.google.common.cache.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*

class CwtTemplateExpression(
    expressionString: String,
    val snippetExpressions: List<CwtDataExpression>
) : AbstractExpression(expressionString), CwtExpression {
    //allowed: enum[xxx], value[xxx], <xxx>, <modifier>
    val referenceExpressions = snippetExpressions.filterTo(mutableSetOf()) { it.type != CwtDataTypes.Constant }
    
    companion object Resolver {
        val EmptyExpression = CwtTemplateExpression("", emptyList())
        
        // job_<job>_add
        // xxx_value[xxx]_xxx
        
        private val cache = CacheBuilder.newBuilder().buildCache<String, CwtTemplateExpression> { doResolve(it) }
        
        fun resolve(expressionString: String): CwtTemplateExpression {
            return cache.get(expressionString)
        }
        
        private fun doResolve(expressionString: String): CwtTemplateExpression {
            return when {
                expressionString.isEmpty() -> EmptyExpression
                expressionString.containsBlank() -> EmptyExpression //不允许包含空白（同时防止后续的处理逻辑出现意外错误）
                else -> {
                    var snippets: MutableList<CwtDataExpression>? = null
                    var startIndex = 0
                    var i1: Int
                    var i2: Int
                    while(true) {
                        i1 = expressionString.indexOf('[', startIndex)
                        if(i1 != -1) {
                            //预先排除 - 应当直接使用enum[xxx]
                            if(expressionString.contains("complex_enum[")) return EmptyExpression
                            
                            i1 = expressionString.indexOf("enum[", startIndex)
                            if(i1 != -1) {
                                i2 = expressionString.indexOf(']', i1 + 5)
                                if(i2 == -1) return EmptyExpression //error
                                val nextIndex = i2 + 1
                                if(i1 == 0 && nextIndex == expressionString.length) return EmptyExpression
                                if(startIndex != i1) {
                                    if(snippets == null) snippets = mutableListOf()
                                    snippets.add(CwtValueExpression.resolve(expressionString.substring(startIndex, i1)))
                                }
                                if(snippets == null) snippets = mutableListOf()
                                snippets.add(CwtValueExpression.resolve(expressionString.substring(i1, nextIndex)))
                                startIndex = nextIndex
                                continue
                            }
                            i1 = expressionString.indexOf("value[", startIndex)
                            if(i1 != -1) {
                                i2 = expressionString.indexOf(']', i1 + 6)
                                if(i2 == -1) return EmptyExpression //error
                                val nextIndex = i2 + 1
                                if(i1 == 0 && nextIndex == expressionString.length) return EmptyExpression
                                if(startIndex != i1) {
                                    if(snippets == null) snippets = mutableListOf()
                                    snippets.add(CwtValueExpression.resolve(expressionString.substring(startIndex, i1)))
                                }
                                if(snippets == null) snippets = mutableListOf()
                                snippets.add(CwtValueExpression.resolve(expressionString.substring(i1, nextIndex)))
                                startIndex = nextIndex
                                continue
                            }
                            return EmptyExpression
                        }
                        i1 = expressionString.indexOf('<', startIndex)
                        if(i1 != -1) {
                            i2 = expressionString.indexOf('>', i1 + 1)
                            if(i2 == -1) return EmptyExpression //error
                            val nextIndex = i2 + 1
                            if(i1 == 0 && nextIndex == expressionString.length) return EmptyExpression
                            if(startIndex != i1) {
                                if(snippets == null) snippets = mutableListOf()
                                snippets.add(CwtValueExpression.resolve(expressionString.substring(startIndex, i1)))
                            }
                            if(snippets == null) snippets = mutableListOf()
                            snippets.add(CwtValueExpression.resolve(expressionString.substring(i1, nextIndex)))
                            startIndex = nextIndex
                            continue
                        }
                        if(startIndex == 0) return EmptyExpression
                        break
                    }
                    if(snippets == null) return EmptyExpression
                    if(startIndex != expressionString.length) {
                        snippets.add(CwtValueExpression.resolve(expressionString.substring(startIndex)))
                    }
                    CwtTemplateExpression(expressionString, snippets)
                }
            }
        }
    }
}
