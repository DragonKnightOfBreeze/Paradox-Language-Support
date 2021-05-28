package icu.windea.pls.script.codeInsight

import com.intellij.codeInsight.daemon.*
import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.psi.*
import com.intellij.ui.awt.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

class ParadoxDefinitionLineMarkerProvider : LineMarkerProviderDescriptor() {
	companion object {
		private val _name = message("paradox.script.gutterIcon.definition")
		private val _title = message("paradox.script.gutterIcon.definition.title")
	}
	
	override fun getName() = _name
	
	override fun getIcon() = definitionGutterIcon
	
	override fun getLineMarkerInfo(element: PsiElement): LineMarker? {
		return when(element) {
			//必须是scriptProperty，且对应一个definition
			is ParadoxScriptProperty -> {
				val definition = element.paradoxDefinitionInfo ?: return null
				LineMarker(element, definition)
			}
			else -> null
		}
	}
	
	class LineMarker(element: ParadoxScriptProperty,definitionInfo: ParadoxDefinitionInfo) : LineMarkerInfo<PsiElement>(
		element.propertyKey.let { it.propertyKeyId ?: it.quotedPropertyKeyId!! },
		element.textRange,
		definitionGutterIcon,
		{
			buildString {
				val name = definitionInfo.name.ifEmpty { anonymousString }
				val typeText = definitionInfo.typeText
				append("(definition) <b>").append(name.escapeXml()).append("</b>: ").append(typeText)
			}
		},
		{ mouseEvent, _ ->
			val project = element.project
			val elements = findDefinitions(definitionInfo.name,definitionInfo.type,project).toTypedArray()
			when(elements.size) {
				0 -> {}
				1 -> OpenSourceUtil.navigate(true, elements.first())
				else -> NavigationUtil.getPsiElementPopup(elements, _title).show(RelativePoint(mouseEvent))
			}
		},
		GutterIconRenderer.Alignment.LEFT,
		{ _name }
	)
}