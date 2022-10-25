package icu.windea.pls.script.navigation

import icu.windea.pls.core.model.*
import icu.windea.pls.core.navigation.*
import icu.windea.pls.script.psi.*

class ParadoxScriptFilePresentation(
	element: ParadoxScriptFile
) : ParadoxItemPresentation<ParadoxScriptFile>(element)

class ParadoxScriptPropertyPresentation(
	element: ParadoxScriptProperty
) : ParadoxItemPresentation<ParadoxScriptProperty>(element)

class ParadoxDefinitionPresentation(
	element: ParadoxScriptProperty,
	private val definitionInfo: ParadoxDefinitionInfo
) : ParadoxItemPresentation<ParadoxScriptProperty>(element) {
	override fun getPresentableText(): String {
		return definitionInfo.name
	}
}

class ParadoxComplexEnumValuePresentation(
	element: ParadoxScriptExpressionElement,
	private val complexEnumValueInfo: ParadoxComplexEnumValueInfo
): ParadoxItemPresentation<ParadoxScriptExpressionElement>(element){
	override fun getPresentableText(): String {
		return complexEnumValueInfo.name
	}
}

class ParadoxScriptVariablePresentation(
	element: ParadoxScriptVariable
) : ParadoxItemPresentation<ParadoxScriptVariable>(element)