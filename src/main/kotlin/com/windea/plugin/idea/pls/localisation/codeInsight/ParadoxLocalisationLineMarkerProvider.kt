package com.windea.plugin.idea.pls.localisation.codeInsight

import com.intellij.codeInsight.daemon.*
import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.psi.*
import com.intellij.ui.awt.*
import com.intellij.util.*
import com.windea.plugin.idea.pls.*
import com.windea.plugin.idea.pls.localisation.psi.*

class ParadoxLocalisationLineMarkerProvider : LineMarkerProviderDescriptor() {
	companion object {
		private val _name = message("paradox.localisation.gutterIcon.localisation")
		private val _title = message("paradox.localisation.gutterIcon.localisation.title")
	}
	
	override fun getName() = _name
	
	override fun getIcon() = localisationGutterIcon
	
	override fun getLineMarkerInfo(element: PsiElement): LineMarker? {
		return when(element) {
			//必须是localisationProperty
			is ParadoxLocalisationProperty -> {
				if(element.paradoxFileInfo == null) return null
				LineMarker(element)
			}
			else -> null
		}
	}
	
	class LineMarker(element: ParadoxLocalisationProperty) : LineMarkerInfo<PsiElement>(
		element.propertyKey.propertyKeyId,
		element.textRange,
		localisationGutterIcon,
		{
			buildString {
				append("(localisation) <b>").append(it.text.unquote()).append("</b>")
			}
		},
		{ mouseEvent, _ ->
			val name = element.name
			val project = element.project
			val elements = findLocalisations(name, null, project,hasDefault=true).toTypedArray()
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