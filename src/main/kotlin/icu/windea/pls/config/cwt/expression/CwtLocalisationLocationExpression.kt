package icu.windea.pls.config.cwt.expression

import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.config.internal.config.*
import icu.windea.pls.localisation.psi.*

/**
 * CWT本地化的位置表达式。
 *
 * 用于推断定义的相关本地化（relatedLocation）的位置。
 *
 * 示例：`"$"`, `"$_desc"`
 * @property placeholder 占位符（表达式文本包含"$"时，为整个字符串，"$"会在解析时替换成definitionName）。
 */
class CwtLocalisationLocationExpression(
	expressionString: String,
	val placeholder: String
) : AbstractExpression(expressionString), CwtExpression {
	companion object Resolver : CachedExpressionResolver<CwtLocalisationLocationExpression>() {
		val EmptyExpression = CwtLocalisationLocationExpression("", "")
		
		override fun doResolve(expressionString: String): CwtLocalisationLocationExpression {
			return when {
				expressionString.isEmpty() -> EmptyExpression
				else -> CwtLocalisationLocationExpression(expressionString, expressionString)
			}
		}
	}
	
	operator fun component1() = placeholder
	
	//localisationKey - localisation(s)
	
	fun resolve(definitionName: String, localeConfig: ParadoxLocaleConfig? = null, project: Project): Pair<String,ParadoxLocalisationProperty?> {
		val key = buildString { for(c in placeholder) if(c == '$') append(definitionName) else append(c) }
		val localisation = findLocalisation(key, localeConfig, project, hasDefault = true)
		return key to localisation
	}
	
	fun resolveAll(definitionName: String, localeConfig: ParadoxLocaleConfig? = null, project: Project): Pair<String, List<ParadoxLocalisationProperty>> {
		val key = buildString { for(c in placeholder) if(c == '$') append(definitionName) else append(c) }
		val localisations = findLocalisations(key, localeConfig, project, hasDefault = true)
		return key to localisations
	}
}

