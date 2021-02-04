package com.windea.plugin.idea.paradox.script.editor

import com.intellij.codeInsight.daemon.*
import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.psi.*
import com.intellij.ui.awt.*
import com.intellij.util.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.psi.*

class ParadoxDefinitionLineMarkerProvider : LineMarkerProviderDescriptor() {
	companion object {
		private val _name = message("paradox.script.gutterIcon.definition")
		private val _title = message("paradox.script.gutterIcon.definition.title")
		private fun _tooltip(name: String,type:String) = message("paradox.script.gutterIcon.definition.tooltip", name,type)
	}
	
	override fun getName() = _name
	
	override fun getIcon() = definitionGutterIcon
	
	override fun getLineMarkerInfo(element: PsiElement): LineMarker? {
		return when(element) {
			is ParadoxScriptProperty -> {
				val typeInfo = element.paradoxTypeInfo ?: return null
				LineMarker(element, typeInfo)
			}
			else -> null
		}
	}
	
	class LineMarker(element: ParadoxScriptProperty,typeInfo: ParadoxTypeInfo) : LineMarkerInfo<PsiElement>(
		element.propertyKey.let { it.propertyKeyId ?: it.quotedPropertyKeyId!! },
		element.textRange,
		definitionGutterIcon,
		{ 
			val name = typeInfo.name.escapeXml()
			val type = buildString{
				append(typeInfo.type)
				if(typeInfo.subtypes.isNotEmpty()){ typeInfo.subtypes.joinTo(this,", ",", ")}
			}
			_tooltip(name, type) 
		},
		{ mouseEvent, _ ->
			val project = element.project
			val elements = findDefinitions(typeInfo.name,typeInfo.type,project).toTypedArray()
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