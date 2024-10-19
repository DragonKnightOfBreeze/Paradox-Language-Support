package icu.windea.pls.config.expression

import com.google.common.cache.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*

// job_<job>_add
// xxx_value[xxx]_xxx

interface CwtTemplateExpression : CwtExpression {
    val snippetExpressions: List<CwtDataExpression>
    //allowed: enum[xxx], value[xxx], <xxx>, <modifier>
    val referenceExpressions: Set<CwtDataExpression>

    companion object Resolver {
        val EmptyExpression: CwtTemplateExpression = doResolveEmpty()

        fun resolve(expressionString: String): CwtTemplateExpression = cache.get(expressionString)
    }
}

//Implementations (cached)

private val cache = CacheBuilder.newBuilder().buildCache<String, CwtTemplateExpression> { doResolve(it) }

private fun doResolveEmpty() = CwtTemplateExpressionImpl("", emptyList())

private fun doResolve(expressionString: String): CwtTemplateExpression {
    return when {
        expressionString.isEmpty() -> doResolveEmpty()
        expressionString.containsBlank() -> doResolveEmpty() //不允许包含空白（同时防止后续的处理逻辑出现意外错误）
        else -> {
            var snippets: MutableList<CwtDataExpression>? = null
            var startIndex = 0
            var i1: Int
            var i2: Int
            while (true) {
                i1 = expressionString.indexOf('[', startIndex)
                if (i1 != -1) {
                    //预先排除 - 应当直接使用enum[xxx]
                    if (expressionString.contains("complex_enum[")) return doResolveEmpty()

                    i1 = expressionString.indexOf("enum[", startIndex)
                    if (i1 != -1) {
                        i2 = expressionString.indexOf(']', i1 + 5)
                        if (i2 == -1) return doResolveEmpty() //error
                        val nextIndex = i2 + 1
                        if (i1 == 0 && nextIndex == expressionString.length) return doResolveEmpty()
                        if (startIndex != i1) {
                            if (snippets == null) snippets = mutableListOf()
                            snippets.add(CwtDataExpression.resolve(expressionString.substring(startIndex, i1), false))
                        }
                        if (snippets == null) snippets = mutableListOf()
                        snippets.add(CwtDataExpression.resolve(expressionString.substring(i1, nextIndex), false))
                        startIndex = nextIndex
                        continue
                    }
                    i1 = expressionString.indexOf("value[", startIndex)
                    if (i1 != -1) {
                        i2 = expressionString.indexOf(']', i1 + 6)
                        if (i2 == -1) return doResolveEmpty() //error
                        val nextIndex = i2 + 1
                        if (i1 == 0 && nextIndex == expressionString.length) return doResolveEmpty()
                        if (startIndex != i1) {
                            if (snippets == null) snippets = mutableListOf()
                            snippets.add(CwtDataExpression.resolve(expressionString.substring(startIndex, i1), false))
                        }
                        if (snippets == null) snippets = mutableListOf()
                        snippets.add(CwtDataExpression.resolve(expressionString.substring(i1, nextIndex), false))
                        startIndex = nextIndex
                        continue
                    }
                    return doResolveEmpty()
                }
                i1 = expressionString.indexOf('<', startIndex)
                if (i1 != -1) {
                    i2 = expressionString.indexOf('>', i1 + 1)
                    if (i2 == -1) return doResolveEmpty() //error
                    val nextIndex = i2 + 1
                    if (i1 == 0 && nextIndex == expressionString.length) return doResolveEmpty()
                    if (startIndex != i1) {
                        if (snippets == null) snippets = mutableListOf()
                        snippets.add(CwtDataExpression.resolve(expressionString.substring(startIndex, i1), false))
                    }
                    if (snippets == null) snippets = mutableListOf()
                    snippets.add(CwtDataExpression.resolve(expressionString.substring(i1, nextIndex), false))
                    startIndex = nextIndex
                    continue
                }
                if (startIndex == 0) return doResolveEmpty()
                break
            }
            if (snippets == null) return doResolveEmpty()
            if (startIndex != expressionString.length) {
                snippets.add(CwtDataExpression.resolve(expressionString.substring(startIndex), false))
            }
            CwtTemplateExpressionImpl(expressionString, snippets)
        }
    }
}

private class CwtTemplateExpressionImpl(
    override val expressionString: String,
    override val snippetExpressions: List<CwtDataExpression>
) : CwtTemplateExpression {
    override val referenceExpressions: Set<CwtDataExpression> = snippetExpressions.filterTo(mutableSetOf()) { it.type != CwtDataTypes.Constant }

    override fun equals(other: Any?) = this === other || other is CwtTemplateExpression && expressionString == other.expressionString
    override fun hashCode() = expressionString.hashCode()
    override fun toString() = expressionString
}
