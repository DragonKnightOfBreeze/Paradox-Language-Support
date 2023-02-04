package icu.windea.pls.script.navigation

import icons.*
import icu.windea.pls.core.navigation.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import javax.swing.*

class ParadoxScriptFilePresentation(
	element: ParadoxScriptFile
) : ParadoxItemPresentation<ParadoxScriptFile>(element)

class ParadoxScriptScriptedVariablePresentation(
	element: ParadoxScriptScriptedVariable
) : ParadoxItemPresentation<ParadoxScriptScriptedVariable>(element)

class ParadoxDefinitionPresentation(
	element: ParadoxScriptProperty,
	private val definitionInfo: ParadoxDefinitionInfo
) : ParadoxItemPresentation<ParadoxScriptProperty>(element) {
	override fun getIcon(unused: Boolean): Icon {
		return PlsIcons.Definition
	}
	
	override fun getPresentableText(): String {
		return definitionInfo.name
	}
}

class ParadoxComplexEnumValuePresentation(
	element: ParadoxScriptStringExpressionElement,
	private val complexEnumValueInfo: icu.windea.pls.lang.model.ParadoxComplexEnumValueInfo
) : ParadoxItemPresentation<ParadoxScriptStringExpressionElement>(element) {
	override fun getIcon(unused: Boolean): Icon {
		return PlsIcons.ComplexEnumValue
	}
	
	override fun getPresentableText(): String {
		return complexEnumValueInfo.name
	}
}