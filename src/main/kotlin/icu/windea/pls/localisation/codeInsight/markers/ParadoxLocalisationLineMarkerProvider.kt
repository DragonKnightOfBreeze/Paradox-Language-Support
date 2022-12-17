package icu.windea.pls.localisation.codeInsight.markers

import com.intellij.codeInsight.daemon.*
import com.intellij.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.psi.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.actions.*
import icu.windea.pls.core.model.ParadoxLocalisationCategory.*
import icu.windea.pls.core.navigation.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.localisation.psi.*

/**
 * 本地化（localisation/localisation_synced）的装订线图标提供器。
 */
class ParadoxLocalisationLineMarkerProvider : RelatedItemLineMarkerProvider() {
	override fun getName() = PlsBundle.message("localisation.gutterIcon.localisation")
	
	override fun getIcon() = PlsIcons.Gutter.Localisation
	
	override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
		//何时显示装订线图标：element是localisation/localisation_synced
		if(element is ParadoxLocalisationProperty) {
			val name = element.name
			val category = element.category ?: return
			
			val icon = PlsIcons.Gutter.Localisation
			val tooltip = buildString {
				append("$category <b>").append(name).append("</b>")
			}
			val project = element.project
			val selector = localisationSelector().gameTypeFrom(element).preferRootFrom(element).preferLocale(preferredParadoxLocale())
			val targets = when(category) {
				Localisation -> findLocalisations(name, project, selector = selector)
				SyncedLocalisation -> findSyncedLocalisations(name, project, selector = selector)
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
			//NavigateAction.setNavigateAction(
			//	lineMarkerInfo,
			//	PlsBundle.message("localisation.gutterIcon.localisation.action"),
			//	PlsActions.GutterGotoLocalisation
			//)
			result.add(lineMarkerInfo)
		}
	}
	
	private fun createGotoRelatedItem(targets: Collection<ParadoxLocalisationProperty>): Collection<GotoRelatedItem> {
		return ParadoxGotoRelatedItem.createItems(targets, PlsBundle.message("localisation.gutterIcon.localisation.group"))
	}
}