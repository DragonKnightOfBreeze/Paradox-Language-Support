package com.windea.plugin.idea.paradox.script.codeInsight

import com.intellij.lang.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.psi.*
import com.windea.plugin.idea.paradox.script.psi.ParadoxScriptTypes.*

class ParadoxScriptExpressionTypeProvider:ExpressionTypeProvider<ParadoxScriptNamedElement>() {
	companion object{
		private val noExpressionFoundMessage = message("noExpressionFound")
	}
	
	override fun getInformationHint(element: ParadoxScriptNamedElement): String {
		return when{
			element is ParadoxScriptVariable -> getVariableHint(element)
			element is ParadoxScriptProperty -> {
				val definitionInfo = element.paradoxDefinitionInfo
				if(definitionInfo != null) getDefinitionHint(definitionInfo) else getPropertyHint(element)
			}
			else -> "(unknown)"
		}
	}
	
	private fun getVariableHint(element:ParadoxScriptVariable):String{
		return element.variableValue?.value?.getType()?:"(unknown)"
	}
	
	private fun getPropertyHint(element:ParadoxScriptProperty):String{
		return element.propertyValue?.value?.getType()?:"(unknown)"
	}
	
	private fun getDefinitionHint(paradoxDefinitionInfo:ParadoxDefinitionInfo):String{
		return buildString{
			append(paradoxDefinitionInfo.type)
			if(paradoxDefinitionInfo.subtypes.isNotEmpty()) paradoxDefinitionInfo.subtypes.joinTo(this,", ",", ")
		}
	}
	
	override fun getErrorHint(): String {
		return noExpressionFoundMessage
	}
	
	override fun getExpressionsAt(elementAt: PsiElement): List<ParadoxScriptNamedElement> {
		val element = when(elementAt.elementType){
			VARIABLE_NAME_ID -> elementAt.parent?.parent as? ParadoxScriptVariable
			VARIABLE_NAME -> elementAt.parent as? ParadoxScriptVariable
			VARIABLE -> elementAt as? ParadoxScriptVariable
			VARIABLE_REFERENCE_ID -> (elementAt.parent as? ParadoxScriptVariableReference)?.reference?.resolve()
			PROPERTY_KEY_ID -> elementAt.parent?.parent as? ParadoxScriptProperty
			PROPERTY_KEY -> elementAt.parent as? ParadoxScriptProperty
			PROPERTY -> (elementAt as? ParadoxScriptProperty)?.reference?.resolve()
			STRING_TOKEN -> (elementAt.parent as? ParadoxScriptString)?.reference?.resolve() as? ParadoxScriptProperty 
			QUOTED_STRING_TOKEN -> (elementAt.parent as? ParadoxScriptString)?.reference?.resolve() as? ParadoxScriptProperty 
			else -> null
		}
		return when {
			element is ParadoxScriptVariable -> return element.toSingletonList()
			element is ParadoxScriptProperty -> return element.toSingletonList()
			else -> emptyList()
		}
	}
}