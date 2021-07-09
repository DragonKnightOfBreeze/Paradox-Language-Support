package icu.windea.pls.localisation.codeInsight

import com.intellij.codeInsight.daemon.*
import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.ParadoxLocalisationCategory.*

class ParadoxLocalisationLineMarkerProvider : RelatedItemLineMarkerProvider() {
	companion object {
		private val _name = message("paradox.localisation.gutterIcon.localisation")
		private val _title = message("paradox.localisation.gutterIcon.localisation.title")
	}
	
	override fun getName() = _name
	
	override fun getIcon() = localisationGutterIcon
	
	override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
		//如果是localisation或localisation_synced，则添加对应的gutterIcon
		if(element is ParadoxLocalisationProperty) {
			val name = element.name
			val category = element.category ?: return
			val lineMarkerInfo = createMarker(element, name, category)
			result.add(lineMarkerInfo)
		}
	}
	
	private fun createMarker(element: ParadoxLocalisationProperty, name: String, category: ParadoxLocalisationCategory): RelatedItemLineMarkerInfo<PsiElement> {
		val icon = localisationGutterIcon
		val tooltip = buildString {
			append("(${category.key}) <b>").append(name).append("</b>")
		}
		val project = element.project
		val targets = when(category) {
			Localisation -> findLocalisations(name, null, project, hasDefault = true)
			SyncedLocalisation -> findSyncedLocalisations(name, null, project, hasDefault = true)
		}
		val targetElement = element.propertyKey.propertyKeyId
		return NavigationGutterIconBuilder.create(icon)
			.setTooltipText(tooltip)
			.setPopupTitle(_title)
			.setTargets(targets)
			.setAlignment(GutterIconRenderer.Alignment.RIGHT)
			.setNamer { _name }
			.createLineMarkerInfo(targetElement)
	}
}