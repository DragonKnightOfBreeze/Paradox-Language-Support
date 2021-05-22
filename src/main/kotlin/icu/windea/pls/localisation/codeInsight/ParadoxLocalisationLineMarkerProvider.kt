package icu.windea.pls.localisation.codeInsight

import com.intellij.codeInsight.daemon.*
import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.psi.*
import com.intellij.ui.awt.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

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
				//所在的根目录必须是"localisation"或"localisation_snyced"
				if(!element.isInValidRootDirectory()) return null
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