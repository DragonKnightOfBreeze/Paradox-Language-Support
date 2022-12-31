package icu.windea.pls.config.cwt.expression

import com.google.common.cache.*
import com.intellij.openapi.project.*
import icu.windea.pls.core.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
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
	companion object Resolver {
		val EmptyExpression = CwtLocalisationLocationExpression("", "")
		
		val cache by lazy { CacheBuilder.newBuilder().buildCache<String, CwtLocalisationLocationExpression> { doResolve(it) } }
		
		fun resolve(expressionString: String) = cache.getUnchecked(expressionString)
		
		private fun doResolve(expressionString: String) = when {
			expressionString.isEmpty() -> EmptyExpression
			expressionString.startsWith('#') -> {
				val propertyName = expressionString.substring(1)
				CwtLocalisationLocationExpression(expressionString, null, propertyName)
			}
			else -> CwtLocalisationLocationExpression(expressionString, expressionString)
		}
	}
	
	operator fun component1() = placeholder
	
	operator fun component2() = propertyName
	
	fun resolvePlaceholder(name: String): String? {
		if(placeholder == null) return null
		return buildString { for(c in placeholder) if(c == '$') append(name) else append(c) }
	}
	
	//(localisationKey - localisation(s))
	
	fun resolve(definition: ParadoxScriptDefinitionElement, definitionInfo: ParadoxDefinitionInfo, project: Project, selector: ChainedParadoxSelector<ParadoxLocalisationProperty>): Pair<String, ParadoxLocalisationProperty?>? {
		if(placeholder != null) {
			//如果定义是匿名的，则直接忽略
			if(definitionInfo.isAnonymous) return null
			
			val key = resolvePlaceholder(definitionInfo.name)!!
			val localisation = ParadoxLocalisationSearch.search(key, project, selector = selector).find()
			return key to localisation
		} else if(propertyName != null) {
			val value = definition.findProperty(propertyName)?.findValue<ParadoxScriptString>() ?: return null
			val key = value.stringValue
			val localisation = ParadoxLocalisationSearch.search(key, project, selector = selector).find()
			return key to localisation
		} else {
			return null //不期望的结果
		}
	}
	
	fun resolveAll(definitionName: String, definition: ParadoxScriptDefinitionElement, project: Project, selector: ChainedParadoxSelector<ParadoxLocalisationProperty>): Pair<String, Set<ParadoxLocalisationProperty>>? {
		if(placeholder != null) {
			val key = resolvePlaceholder(definitionName)!!
			val localisations = ParadoxLocalisationSearch.search(key, project, selector = selector).findAll()
			return key to localisations
		} else if(propertyName != null) {
			val value = definition.findProperty(propertyName)?.findValue<ParadoxScriptString>() ?: return null
			val key = value.stringValue
			val localisations = ParadoxLocalisationSearch.search(key, project, selector = selector).findAll()
			return key to localisations
		} else {
			return null //不期望的结果
		}
	}
}

