package com.windea.plugin.idea.paradox.localisation.editor

import com.intellij.codeInsight.daemon.*
import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.psi.*
import com.intellij.ui.awt.*
import com.intellij.util.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.psi.*

class ParadoxLocalisationLineMarkerProvider : LineMarkerProviderDescriptor() {
	companion object {
		private val _name = message("paradox.localisation.gutterIcon.localisation")
		private val _title = message("paradox.localisation.gutterIcon.localisation.title")
		private fun _tooltip(name: String) = message("paradox.localisation.gutterIcon.localisation.tooltip", name)
	}
	
	override fun getName() = _name
	
	override fun getIcon() = localisationGutterIcon
	
	override fun getLineMarkerInfo(element: PsiElement): LineMarker? {
		return when(element) {
			is ParadoxLocalisationProperty -> LineMarker(element)
			else -> null
		}
	}
	
	class LineMarker(element: ParadoxLocalisationProperty) : LineMarkerInfo<PsiElement>(
		element.propertyKey.propertyKeyId,
		element.textRange,
		localisationGutterIcon,
		{ _tooltip(it.text.unquote()) },
		{ mouseEvent, _ ->
			val name = element.name
			val project = element.project
			val elements = findLocalisationProperties(name, null, project).toTypedArray()
			when(elements.size) {
				0 -> { }
				1 -> OpenSourceUtil.navigate(true, elements.first())
				else -> NavigationUtil.getPsiElementPopup(elements, _title).show(RelativePoint(mouseEvent))
			}
		},
		GutterIconRenderer.Alignment.LEFT,
		{ _name }
	)
}