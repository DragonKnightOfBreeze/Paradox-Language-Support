package com.windea.plugin.idea.paradox.script.editor

import com.intellij.codeInsight.daemon.*
import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.psi.*
import com.intellij.ui.awt.*
import com.intellij.util.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.model.*
import com.windea.plugin.idea.paradox.script.psi.*

class ParadoxDefinitionLocalisationLineMarkerProvider : LineMarkerProviderDescriptor() {
	companion object {
		private val _name = message("paradox.script.gutterIcon.definitionLocalisation")
		private val _title = message("paradox.script.gutterIcon.definitionLocalisation.title")
	}
	
	override fun getName() = _name
	
	override fun getIcon() = definitionLocalisationGutterIcon
	
	override fun getLineMarkerInfo(element: PsiElement): LineMarker? {
		return when(element) {
			//必须是scriptProperty，且对应一个definition
			is ParadoxScriptProperty -> {
				val definition = element.paradoxDefinitionInfo ?: return null
				if(definition.localisation.isEmpty()) return null //没有localisation时不加上gutterIcon
				LineMarker(element, definition)
			}
			else -> null
		}
	}
	
	class LineMarker(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo) : LineMarkerInfo<PsiElement>(
		element.propertyKey.let { it.propertyKeyId ?: it.quotedPropertyKeyId!! },
		element.textRange,
		definitionLocalisationGutterIcon,
		{
			buildString {
				val localisation = definitionInfo.resolvedLocalisation
				var isFirst = true
				for((k, v) in localisation) {
					if(isFirst) isFirst = false else appendBr()
					append("(definition localisation) ").append(k).append(" = <b>").appendPsiLink("#", v).append("</b>")
				}
			}
		},
		{ mouseEvent, _ ->
			val names = definitionInfo.resolvedLocalisationNames
			val project = element.project
			val elements = findLocalisations(names, null, project, hasDefault = true, keepOrder = true).toTypedArray()
			when(elements.size) {
				0 -> { }
				1 -> OpenSourceUtil.navigate(true, elements.first())
				else -> NavigationUtil.getPsiElementPopup(elements, _title).show(RelativePoint(mouseEvent))
			}
		},
		GutterIconRenderer.Alignment.RIGHT,
		{ _name }
	)
}