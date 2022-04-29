package icu.windea.pls.localisation.codeInsight.markers

import com.intellij.codeInsight.daemon.*
import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.core.ParadoxLocalisationCategory.*

/**
 * 本地化（localisation/localisation_synced）的装订线图标提供器。
 */
class ParadoxLocalisationLineMarkerProvider : RelatedItemLineMarkerProvider() {
	companion object {
		private val _name = PlsBundle.message("localisation.gutterIcon.localisation")
		private val _title = PlsBundle.message("localisation.gutterIcon.localisation.title")
	}
	
	override fun getName() = _name
	
	override fun getIcon() = PlsIcons.localisationGutterIcon
	
	override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
		//何时显示装订线图标：element是localisation/localisation_synced
		if(element is ParadoxLocalisationProperty) {
			val name = element.name
			val category = element.category ?: return
			
			val icon = PlsIcons.localisationGutterIcon
			val tooltip = buildString {
				append("(${category.id}) <b>").append(name).append("</b>")
			}
			val project = element.project
			val targets = when(category) {
				Localisation -> findLocalisations(name, null, project, hasDefault = true)
				SyncedLocalisation -> findSyncedLocalisations(name, null, project, hasDefault = true)
			}
			val locationElement = element.propertyKey.propertyKeyId
			val lineMarkerInfo = NavigationGutterIconBuilder.create(icon)
				.setTooltipText(tooltip)
				.setPopupTitle(_title)
				.setTargets(targets)
				.setAlignment(GutterIconRenderer.Alignment.RIGHT)
				.setNamer { _name }
				.createLineMarkerInfo(locationElement)
			result.add(lineMarkerInfo)
		}
	}
	
}