package com.windea.plugin.idea.paradox.script.codeInsight

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.psi.*
import com.windea.plugin.idea.paradox.script.psi.*
import com.windea.plugin.idea.paradox.script.psi.ParadoxScriptTypes.*

@Suppress("DialogTitleCapitalization")
class ParadoxScriptExpressionTypeProvider:ExpressionTypeProvider<PsiElement>() {
	companion object{
		private val noExpressionFoundMessage = message("noExpressionFound")
	}
	
	override fun getInformationHint(element: PsiElement): String {
		return when{
			element is ParadoxScriptVariable -> getVariableHint(element)
			element is ParadoxScriptProperty -> {
				val definition = element.paradoxDefinition ?: return getPropertyHint(element)
				getDefinitionHint(definition) 
			}
			element is ParadoxScriptVariableReference -> {
				val e = element.reference.resolve()?: return "(unknown)"
				getVariableHint(e) 
			}
			element is ParadoxScriptString -> {
				val e = element.reference.resolve()?: return "(unknown)"
				when{
					e is ParadoxScriptProperty -> {
						val definition = e.paradoxDefinition?:return "string"
						getDefinitionHint(definition) 
					}
					e is ParadoxLocalisationProperty -> "localisation"
					else -> "string"
				}
			}
			element is ParadoxScriptValue -> element.getType() ?: "(unknown)"
			else -> "(unknown)"
		}
	}
	
	private fun getVariableHint(element:ParadoxScriptVariable):String{
		return element.variableValue?.value?.getType()?:"(unknown)"
	}
	
	private fun getPropertyHint(element:ParadoxScriptProperty):String{
		return element.propertyValue?.value?.getType()?:"(unknown)"
	}
	
	private fun getDefinitionHint(definition:ParadoxDefinition):String{
		return buildString{
			val (_,type,subtypes) = definition
			appendType(type,subtypes)
		}
	}
	
	override fun getErrorHint(): String {
		return noExpressionFoundMessage
	}
	
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
			NUMBER_TOKEN -> elementAt.parent as? ParadoxScriptNumber
			NUMBER -> elementAt as? ParadoxScriptNumber
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