package icu.windea.pls.localisation.codeInsight.markers

import com.intellij.codeInsight.daemon.*
import com.intellij.codeInsight.navigation.*
import com.intellij.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.core.ParadoxLocalisationCategory.*

/**
 * 本地化（localisation/localisation_synced）的装订线图标提供器。
 */
class ParadoxLocalisationLineMarkerProvider : RelatedItemLineMarkerProvider() {
	override fun getName() = PlsBundle.message("localisation.gutterIcon.localisation")
	
	override fun getIcon() = PlsIcons.localisationGutterIcon
	
	override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
		//何时显示装订线图标：element是localisation/localisation_synced
		if(element is ParadoxLocalisationProperty) {
			val name = element.name
			val category = element.category ?: return
			
			val icon = PlsIcons.localisationGutterIcon
			val tooltip = buildString {
				append("$category <b>").append(name).append("</b>")
			}
			val project = element.project
			val targets = when(category) {
				Localisation -> findLocalisations(name, null, project, hasDefault = true)
				SyncedLocalisation -> findSyncedLocalisations(name, null, project, hasDefault = true)
			}
			if(targets.isEmpty()) return
			val locationElement = element.propertyKey.propertyKeyId
			val lineMarkerInfo = createNavigationGutterIconBuilder(icon){ createGotoRelatedItem(targets)}
				.setTooltipText(tooltip)
				.setPopupTitle(PlsBundle.message("localisation.gutterIcon.localisation.title"))
				.setTargets(targets)
				.setAlignment(GutterIconRenderer.Alignment.RIGHT)
				.setNamer { PlsBundle.message("localisation.gutterIcon.localisation") }
				.createLineMarkerInfo(locationElement)
			result.add(lineMarkerInfo)
		}
	}
	
	private fun createGotoRelatedItem(targets: List<ParadoxLocalisationProperty>): Collection<GotoRelatedItem> {
		return GotoRelatedItem.createItems(targets, PlsBundle.message("localisation.gutterIcon.localisation.group"))
	}
}