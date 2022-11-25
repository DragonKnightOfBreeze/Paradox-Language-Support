package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxValueSetValueExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange,
	val expressions: List<CwtDataExpression>,
	val configGroup: CwtConfigGroup
) : ParadoxScriptExpressionNode {
	override fun getAttributesKey(): TextAttributesKey? {
		if(text.isParameterAwareExpression()) return null
		val expression = expressions.first() //first is ok
		val valueSetName = expression.value ?: return null
		return when(valueSetName) {
			"variable" -> ParadoxScriptAttributesKeys.VARIABLE_KEY
			else -> ParadoxScriptAttributesKeys.VALUE_SET_VALUE_KEY
		}
	}
	
	override fun getReference(element: ParadoxScriptExpressionElement): Reference? {
		if(text.isParameterAwareExpression()) return null
		return Reference(element, rangeInExpression, text, expressions, configGroup)
	}
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, expressions: List<CwtDataExpression>, configGroup: CwtConfigGroup): ParadoxValueSetValueExpressionNode? {
			//text may contain parameters
			if(expressions.any { it.type != CwtDataTypes.Value && it.type != CwtDataTypes.ValueSet }) return null
			return ParadoxValueSetValueExpressionNode(text, textRange, expressions, configGroup)
		}
	}
	
	class Reference(
		element: ParadoxScriptExpressionElement,
		rangeInElement: TextRange,
		private val name: String,
		private val expressions: List<CwtDataExpression>,
		private val configGroup: CwtConfigGroup
	) : PsiReferenceBase<ParadoxScriptExpressionElement>(element, rangeInElement) {
		override fun handleElementRename(newElementName: String): ParadoxScriptExpressionElement {
			val resolved = resolve()
			when {
				resolved == null -> pass()
				resolved.language == CwtLanguage -> throw IncorrectOperationException() //不允许重命名
			}
			//重命名当前元素（仅修改对应范围的文本，认为整个文本没有用引号括起）
			return element.setValue(rangeInElement.replace(element.value, newElementName))
		}
		
		override fun resolve(): PsiElement? {
			return CwtConfigHandler.resolveValueSetValue(element, name, expressions, configGroup)
		}
	}
}
