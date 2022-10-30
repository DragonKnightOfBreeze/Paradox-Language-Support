package icu.windea.pls.script.expression

import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.script.psi.*

/**
 * 脚本表达式。
 */
interface   ParadoxScriptExpression : Expression {
	val value: String get() = expressionString
	val quoted: Boolean
	val type: ParadoxScriptExpressionType
	val isKey: Boolean?
	
	companion object {
		fun resolve(element: ParadoxScriptPropertyKey): ParadoxScriptSimpleExpression {
			return ParadoxScriptSimpleExpression(element.value, element.isQuoted(), element.expressionType, true)
		}
		
		fun resolve(element: ParadoxScriptValue): ParadoxScriptSimpleExpression {
			return ParadoxScriptSimpleExpression(element.value, element.isQuoted(), element.expressionType, false)
		}
		
		fun resolve(expressionString: String, isQuoted: Boolean, isKey: Boolean? = null): ParadoxScriptSimpleExpression {
			val expressionType = ParadoxScriptExpressionType.resolve(expressionString)
			return ParadoxScriptSimpleExpression(expressionString, isQuoted, expressionType, isKey)
		}
		
		fun resolve(text: String, isKey: Boolean? = null): ParadoxScriptSimpleExpression {
			val expressionString = text.unquote()
			val quoted = text.isQuoted()
			val expressionType = ParadoxScriptExpressionType.resolve(text)
			return ParadoxScriptSimpleExpression(expressionString, quoted, expressionType, isKey)
		}
		
		fun resolveScopeField(expressionString: String, configGroup: CwtConfigGroup): ParadoxScriptScopeFieldExpression {
			return ParadoxScriptScopeFieldExpression.resolve(expressionString, configGroup)
		}
		
		fun resolveValueField(expressionString: String, configGroup: CwtConfigGroup): ParadoxScriptValueFieldExpression {
			return ParadoxScriptValueFieldExpression.resolve(expressionString, configGroup)
		}
		
		fun resolveValueSetValue(expressionString: String, configGroup: CwtConfigGroup): ParadoxScriptValueSetValueExpression {
			return ParadoxScriptValueSetValueExpression.resolve(expressionString, configGroup)
		}
	}
}