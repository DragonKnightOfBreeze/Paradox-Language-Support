package icu.windea.pls.config.cwt.expression

import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.annotations.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

/**
 * CWT本地化的位置表达式。
 *
 * 用于推断定义的相关本地化（relatedLocation）的位置。
 *
 * 示例：`"$"`, `"$_desc"`, `#title`
 * @property placeholder 占位符（表达式文本包含"$"时，为整个字符串，"$"会在解析时替换成定义的名字，如果定义是匿名的，则忽略此表达式）。
 * @property propertyName 属性名（表达式文本以"#"开始时，为"#"之后的子字符串，可以为空字符串）。
 */
class CwtLocalisationLocationExpression(
	expressionString: String,
	val placeholder: String? = null,
	val propertyName: String? = null
) : AbstractExpression(expressionString), CwtExpression {
	companion object Resolver : CachedExpressionResolver<CwtLocalisationLocationExpression>() {
		val EmptyExpression = CwtLocalisationLocationExpression("", "")
		
		override fun doResolve(expressionString: String): CwtLocalisationLocationExpression {
			return when {
				expressionString.isEmpty() -> EmptyExpression
				expressionString.startsWith('#') -> {
					val propertyName = expressionString.substring(1)
					CwtLocalisationLocationExpression(expressionString, null, propertyName)
				}
				else -> CwtLocalisationLocationExpression(expressionString, expressionString)
			}
		}
	}
	
	operator fun component1() = placeholder
	
	operator fun component2() = propertyName
	
	fun resolvePlaceholder(name: String): String? {
		if(placeholder == null) return null
		return buildString { for(c in placeholder) if(c == '$') append(name) else append(c) }
	}
	
	//(localisationKey - localisation(s))
	
	@RunInReadAction
	fun resolve(definition: ParadoxDefinitionProperty, definitionInfo: ParadoxDefinitionInfo, project: Project, selector: ChainedParadoxSelector<ParadoxLocalisationProperty>): Pair<String, ParadoxLocalisationProperty?>? {
		if(placeholder != null) {
			//如果定义是匿名的，则直接忽略
			if(definitionInfo.isAnonymous) return null
			
			val key = resolvePlaceholder(definitionInfo.name)!!
			val localisation = findLocalisation(key, project, selector = selector)
			return key to localisation
		} else if(propertyName != null) {
			val value = definition.findTargetElement(propertyName)?.findPropertyValue<ParadoxScriptString>() ?: return null
			val key = value.stringValue
			val localisation = findLocalisation(key, project, selector = selector)
			return key to localisation
		} else {
			return null //不期望的结果
		}
	}
	
	fun resolveAll(definitionName: String, definition: ParadoxDefinitionProperty, project: Project, selector: ChainedParadoxSelector<ParadoxLocalisationProperty>): Pair<String, Set<ParadoxLocalisationProperty>>? {
		if(placeholder != null) {
			val key = resolvePlaceholder(definitionName)!!
			val localisations = findLocalisations(key, project, selector = selector)
			return key to localisations
		} else if(propertyName != null) {
			val value = definition.findTargetElement(propertyName)?.findPropertyValue<ParadoxScriptString>() ?: return null
			val key = value.stringValue
			val localisations = findLocalisations(key, project, selector = selector)
			return key to localisations
		} else {
			return null //不期望的结果
		}
	}
}

