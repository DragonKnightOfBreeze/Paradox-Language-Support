package com.windea.plugin.idea.paradox.script.codeInsight

import com.intellij.lang.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.psi.*

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
		return when {
			elementAt is ParadoxScriptProperty -> return elementAt.toSingletonList()
			elementAt is ParadoxScriptPropertyKey -> return (elementAt.parent as? ParadoxScriptProperty).toSingletonListOrEmpty()
			elementAt is ParadoxScriptVariable -> return elementAt.toSingletonListOrEmpty()
			elementAt is ParadoxScriptVariableName -> return (elementAt.parent as? ParadoxScriptVariable).toSingletonListOrEmpty()
			else -> emptyList()
		}
	}
}