package icu.windea.pls.config.cwt.expression

import com.google.common.cache.*
import com.intellij.util.*
import icu.windea.pls.core.*

open class CwtTemplateExpression(
	expressionString: String,
	val snippetExpressions: List<CwtDataExpression>
) : AbstractExpression(expressionString), CwtExpression {
	//allowed: enum[xxx], value[xxx], <xxx>, <modifier>
	val referenceExpressions = snippetExpressions.filterTo(mutableSetOf()) { it -> it.type != CwtDataType.Constant }
	
	fun resolveName(referenceName: String): String{
		if(referenceExpressions.size != 1) throw IllegalStateException()
		return buildString {
			for(snippetExpression in snippetExpressions) {
				when(snippetExpression.type) {
					CwtDataType.Constant -> append(snippetExpression.expressionString)
					else -> append(referenceName)
				}
			}
		}
	}
	
	fun resolveName(referenceNames: Map<CwtDataExpression, String>): String{
		if(referenceExpressions.size != referenceNames.size) throw IllegalStateException()
		return buildString {
			for(snippetExpression in snippetExpressions) {
				when(snippetExpression.type) {
					CwtDataType.Constant -> append(snippetExpression.expressionString)
					else -> append(referenceNames.getValue(snippetExpression))
				}
			}
		}
	}
	
	companion object Resolver {
		val EmptyExpression = CwtTemplateExpression("", emptyList())
		
		val cache by lazy { CacheBuilder.newBuilder().buildCache<String, CwtTemplateExpression> { doResolve(it) } }
		
		// job_<job>_add
		// xxx_value[xxx]_xxx
		
		fun resolve(expressionString: String) = cache.getUnchecked(expressionString)
		
		private fun doResolve(expressionString: String): CwtTemplateExpression {
			return when {
				expressionString.isEmpty() -> EmptyExpression
				else -> {
					val snippets = SmartList<CwtDataExpression>()
					var startIndex = 0
					var i1: Int
					var i2: Int
					while(true) {
						i1 = expressionString.indexOf("enum[", startIndex)
						if(i1 != -1) {
							i2 = expressionString.indexOf(']', i1 + 5)
							if(i2 == -1) return EmptyExpression //error
							val nextIndex = i2 + 1
							if(startIndex != i1) {
								snippets.add(CwtValueExpression.resolve(expressionString.substring(startIndex, i1)))
							}
							snippets.add(CwtValueExpression.resolve(expressionString.substring(i1, nextIndex)))
							startIndex = nextIndex
							continue
						}
						i1 = expressionString.indexOf("value[", startIndex)
						if(i1 != -1) {
							i2 = expressionString.indexOf(']', i1 + 6)
							if(i2 == -1) return EmptyExpression //error
							val nextIndex = i2 + 1
							if(startIndex != i1) {
								snippets.add(CwtValueExpression.resolve(expressionString.substring(startIndex, i1)))
							}
							snippets.add(CwtValueExpression.resolve(expressionString.substring(i1, nextIndex)))
							startIndex = nextIndex
							continue
						}
						i1 = expressionString.indexOf('<', startIndex)
						if(i1 != -1) {
							i2 = expressionString.indexOf('>', i1 + 1)
							if(i2 == -1) return EmptyExpression //error
							val nextIndex = i2 + 1
							if(startIndex != i1) {
								snippets.add(CwtValueExpression.resolve(expressionString.substring(startIndex, i1)))
							}
							snippets.add(CwtValueExpression.resolve(expressionString.substring(i1, nextIndex)))
							startIndex = nextIndex
							continue
						}
						break
					}
					if(snippets.isEmpty()) return EmptyExpression
					CwtTemplateExpression(expressionString, snippets)
				}
			}
		}
	}
}

