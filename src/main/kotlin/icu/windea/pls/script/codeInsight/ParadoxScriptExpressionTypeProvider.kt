package icu.windea.pls.script.codeInsight

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptTypes.*

@Suppress("DialogTitleCapitalization")
class ParadoxScriptExpressionTypeProvider:ExpressionTypeProvider<PsiElement>() {
	companion object{
		private val noExpressionFoundMessage = message("noExpressionFound")
	}
	
	//(definition type)
	//boolean | int | float | string | color | code
	override fun getInformationHint(element: PsiElement): String {
		return when{
			element is ParadoxScriptVariable -> getVariableHint(element)
			element is ParadoxScriptProperty -> {
				val definition = element.paradoxDefinitionInfo ?: return getPropertyHint(element)
				getDefinitionHint(definition) 
			}
			element is ParadoxScriptVariableReference -> {
				val e = element.reference.resolve()?: return "(unknown)"
				getVariableHint(e) 
			}
			//TODO 进一步解析scriptString的具体类型（enum, alias等）
			element is ParadoxScriptValue -> element.getType() ?: "(unknown)"
			else -> "(unknown)"
		}
	}
	
	private fun getVariableHint(element: ParadoxScriptVariable):String{
		return element.variableValue?.value?.getType()?:"(unknown)"
	}
	
	private fun getPropertyHint(element: ParadoxScriptProperty):String{
		return element.propertyValue?.value?.getType()?:"(unknown)"
	}
	
	private fun getDefinitionHint(definitionInfo:ParadoxDefinitionInfo):String{
		return definitionInfo.typeText
	}
	
	override fun getErrorHint(): String {
		return noExpressionFoundMessage
	}
	
	//scriptValue | scriptProperty
	//scriptString | scriptBoolean | scriptInt | scriptFloat | scriptColor | scriptCode | scriptBlock
	override fun getExpressionsAt(elementAt: PsiElement): List<PsiElement> {
		val element = when(elementAt.elementType){
			VARIABLE_NAME_ID -> elementAt.parent?.parent as? ParadoxScriptVariable
			VARIABLE_NAME -> elementAt.parent as? ParadoxScriptVariable
			VARIABLE -> elementAt as? ParadoxScriptVariable
			VARIABLE_REFERENCE_ID -> elementAt.parent as? ParadoxScriptVariableReference
			PROPERTY_KEY_ID -> elementAt.parent?.parent as? ParadoxScriptProperty
			PROPERTY_KEY -> elementAt.parent as? ParadoxScriptProperty
			PROPERTY -> elementAt as? ParadoxScriptProperty
			STRING_TOKEN -> elementAt.parent as? ParadoxScriptString
			QUOTED_STRING_TOKEN -> elementAt.parent as? ParadoxScriptString
			STRING -> elementAt as? ParadoxScriptString
			BOOLEAN_TOKEN -> elementAt.parent as? ParadoxScriptBoolean
			BOOLEAN -> elementAt as? ParadoxScriptBoolean
			INT_TOKEN -> elementAt.parent as? ParadoxScriptInt
			INT -> elementAt as? ParadoxScriptInt
			FLOAT_TOKEN -> elementAt.parent as? ParadoxScriptFloat
			FLOAT -> elementAt as? ParadoxScriptFloat
			COLOR_TOKEN -> elementAt.parent as? ParadoxScriptColor
			COLOR -> elementAt as? ParadoxScriptColor
			CODE_TEXT_TOKEN, CODE_START, CODE_END -> elementAt.parent as? ParadoxScriptCode
			CODE -> elementAt as? ParadoxScriptCode
			LEFT_BRACE, RIGHT_BRACE -> elementAt.parent as? ParadoxScriptBlock
			BLOCK, ROOT_BLOCK -> elementAt as? ParadoxScriptBlock
			else -> null
		}
		return element.toSingletonListOrEmpty()
	}
}