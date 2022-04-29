package icu.windea.pls.config.cwt.expression

import icu.windea.pls.*
import icu.windea.pls.script.psi.*

/**
 * CWT位置表达式。
 *
 * 用于推断relatedLocation的键值，或者relatedPicture的对应sprite的definitionName或DDS文件名。
 *
 * 示例：`"$"`, `"$_desc"`, `"#name"`, "#icon|#icon_frame"`
 * @property placeholder 占位符（表达式文本包含"$"时，为整个字符串，"$"会在解析时替换成definitionName）。
 * @property propertyName 属性名（表达式文本以"#"开始时，为"#"之后和可能的"|"之前的子字符串）。
 * @property extraPropertyNames 额外的属性名（表达式文本以"#"开始且之后包含"|"时，为"|"之后的按","分割的子字符串）。
 */
class CwtLocationExpression(
	expressionString: String,
	val placeholder: String? = null,
	val propertyName: String? = null,
	val extraPropertyNames: List<String>? = null
) : AbstractExpression(expressionString), CwtExpression {
	companion object Resolver : CachedExpressionResolver<CwtLocationExpression>() {
		val EmptyExpression = CwtLocationExpression("")
		
		override fun doResolve(expressionString: String): CwtLocationExpression {
			return when {
				expressionString.isEmpty() -> EmptyExpression
				expressionString.startsWith('#') -> {
					val pipeIndex = expressionString.indexOf('|', 1)
					if(pipeIndex == -1) {
						val propertyName = expressionString.substring(1)
						CwtLocationExpression(expressionString, null, propertyName)
					} else {
						val propertyName = expressionString.substring(1)
						val extraPropertyNames = expressionString.substring(pipeIndex + 1).split(',')
						CwtLocationExpression(expressionString, null, propertyName, extraPropertyNames)
					}
				}
				else -> {
					CwtLocationExpression(expressionString, expressionString, null, null)
				}
			}
		}
	}
	
	operator fun component1() = placeholder
	
	operator fun component2() = propertyName
	
	operator fun component3() = extraPropertyNames
	
	fun inferLocation(definitionName: String, definition: ParadoxDefinitionProperty): String? {
		//TODO 应用extraPropertyNames
		return if(placeholder != null && placeholder.isNotEmpty()) {
			buildString { for(c in placeholder) if(c == '$') append(definitionName) else append(c) }
		} else if(propertyName != null && propertyName.isNotEmpty()) {
			//目前只接收类型为string的值
			definition.findProperty(propertyName)?.propertyValue?.value.castOrNull<ParadoxScriptString>()?.stringValue
		} else {
			null
		}
	}
}