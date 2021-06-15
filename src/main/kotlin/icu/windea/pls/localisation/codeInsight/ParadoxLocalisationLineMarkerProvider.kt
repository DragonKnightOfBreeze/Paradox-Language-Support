package icu.windea.pls.localisation.codeInsight

import com.intellij.codeInsight.daemon.*
import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.psi.*
import com.intellij.ui.awt.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.ParadoxLocalisationCategory.*
import icu.windea.pls.script.codeInsight.*
import icu.windea.pls.script.psi.*

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
			val localisationInfo = element.paradoxLocalisationInfo ?: return
			val lineMarkerInfo = createMarker(localisationInfo, element)
			result.add(lineMarkerInfo)
		}
	}
	
	private fun createMarker(localisationInfo: ParadoxLocalisationInfo, element: ParadoxLocalisationProperty): RelatedItemLineMarkerInfo<PsiElement> {
		val icon = localisationGutterIcon
		val tooltip = buildString {
			val (name, category) = localisationInfo
			append("(${category.key}) <b>").append(name).append("</b>")
		}
		val (name, category) = localisationInfo
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
			.setAlignment(GutterIconRenderer.Alignment.LEFT)
			.setNamer { _name }
			.createLineMarkerInfo(targetElement)
	}
	
	//override fun getLineMarkerInfo(element: PsiElement): LineMarker? {
	//	return when(element) {
	//		//必须是localisationProperty，且必须是localisation或localisation_synced
	//		is ParadoxLocalisationProperty -> {
	//			val localisationInfo = element.paradoxLocalisationInfo?: return null
	//			LineMarker(element,localisationInfo)
	//		}
	//		else -> null
	//	}
	//}
	//
	//class LineMarker(
	//	element: ParadoxLocalisationProperty, 
	//	localisationInfo: ParadoxLocalisationInfo
	//) : LineMarkerInfo<PsiElement>(
	//	element.propertyKey.propertyKeyId,
	//	element.textRange,
	//	localisationGutterIcon,
	//	{
	//		buildString {
	//			val (name, category) = localisationInfo
	//			append("(${category.key}) <b>").append(name).append("</b>")
	//		}
	//	},
	//	{ mouseEvent, _ ->
	//		val (name, category) = localisationInfo
	//		val project = element.project
	//		val elements = when(category){
	//			Localisation -> findLocalisations(name, null, project,hasDefault=true)
	//			SyncedLocalisation -> findSyncedLocalisations(name, null, project,hasDefault=true)
	//		}.toTypedArray()
	//		when(elements.size) {
	//			0 -> {}
	//			1 -> OpenSourceUtil.navigate(true, elements.first())
	//			else -> NavigationUtil.getPsiElementPopup(elements, _title).show(RelativePoint(mouseEvent))
	//		}
	//	},
	//	GutterIconRenderer.Alignment.LEFT,
	//	{ _name }
	//)
}