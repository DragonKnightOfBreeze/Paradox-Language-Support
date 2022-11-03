package icu.windea.pls.script.navigation

import icons.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.navigation.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.*
import javax.swing.*

class ParadoxScriptFilePresentation(
	element: ParadoxScriptFile
) : ParadoxItemPresentation<ParadoxScriptFile>(element)

class ParadoxScriptScriptedVariablePresentation(
	element: ParadoxScriptScriptedVariable
) : ParadoxItemPresentation<ParadoxScriptScriptedVariable>(element)

class ParadoxScriptPropertyPresentation(
	element: ParadoxScriptProperty
) : ParadoxItemPresentation<ParadoxScriptProperty>(element)

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
	element: ParadoxExpressionAwareElement,
	private val complexEnumValueInfo: ParadoxComplexEnumValueInfo
) : ParadoxItemPresentation<ParadoxExpressionAwareElement>(element) {
	override fun getIcon(unused: Boolean): Icon {
		return PlsIcons.ComplexEnumValue
	}
	
	override fun getPresentableText(): String {
		return complexEnumValueInfo.name
	}
}