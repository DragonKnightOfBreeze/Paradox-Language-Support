package icu.windea.pls.script.codeInsight

import com.intellij.codeInsight.daemon.*
import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.psi.*
import com.intellij.ui.awt.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

class ParadoxDefinitionLocalisationLineMarkerProvider : RelatedItemLineMarkerProvider() {
	companion object {
		private val _name = message("paradox.script.gutterIcon.definitionLocalisation")
		private val _title = message("paradox.script.gutterIcon.definitionLocalisation.title")
	}
	
	override fun getName() = _name
	
	override fun getIcon() = definitionLocalisationGutterIcon
	
	override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
		//如果是definition且definition的localisation不为空，则添加definitionLocalisation的gutterIcon
		if(element is ParadoxScriptProperty) {
			val definitionInfo = element.paradoxDefinitionInfo ?: return
			val localisation = definitionInfo.localisation
			if(localisation.isEmpty()) return
			val lineMarkerInfo = createMarker(definitionInfo, element)
			result.add(lineMarkerInfo)
		}
	}
	
	private fun createMarker(definitionInfo: ParadoxDefinitionInfo, element: ParadoxScriptProperty): RelatedItemLineMarkerInfo<PsiElement> {
		val icon = definitionLocalisationGutterIcon
		val tooltip = buildString {
			val localisation = definitionInfo.localisation
			var isFirst = true
			for((n, kn) in localisation) {
				if(kn.isEmpty()) continue //不显示keyName为空的definitionLocalisation
				if(isFirst) isFirst = false else appendBr()
				append("(definition localisation) ").append(n).append(" = <b>").appendPsiLink("#", kn).append("</b>")
			}
		}
		val keyNames = definitionInfo.localisationKeyNames
		val project = element.project
		val targets = findLocalisationsByNames(keyNames, null, project, hasDefault = true, keepOrder = true)
		val targetElement = element.propertyKey.let { it.propertyKeyId ?: it.quotedPropertyKeyId!! }
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
	//		//必须是scriptProperty，且必须是definition，且definition的localisation不能为空
	//		is ParadoxScriptProperty -> {
	//			val definition = element.paradoxDefinitionInfo ?: return null
	//			if(definition.localisation.isEmpty()) return null
	//			LineMarker(element, definition)
	//		}
	//		else -> null
	//	}
	//}
	//
	//class LineMarker(
	//	element: ParadoxScriptProperty,
	//	definitionInfo: ParadoxDefinitionInfo
	//) : LineMarkerInfo<PsiElement>(
	//	element.propertyKey.let { it.propertyKeyId ?: it.quotedPropertyKeyId!! },
	//	element.textRange,
	//	definitionLocalisationGutterIcon,
	//	{
	//		buildString {
	//			val localisation = definitionInfo.localisation
	//			var isFirst = true
	//			for((n, kn) in localisation) {
	//				if(kn.isEmpty()) continue //不显示keyName为空的definitionLocalisation
	//				if(isFirst) isFirst = false else appendBr()
	//				append("(definition localisation) ").append(n).append(" = <b>").appendPsiLink("#", kn).append("</b>")
	//			}
	//		}
	//	},
	//	{ mouseEvent, _ ->
	//		val keyNames = definitionInfo.localisationKeyNames
	//		val project = element.project
	//		val elements = findLocalisationsByNames(keyNames, null, project, hasDefault = true, keepOrder = true).toTypedArray()
	//		when(elements.size) {
	//			0 -> {
	//			}
	//			1 -> OpenSourceUtil.navigate(true, elements.first())
	//			else -> NavigationUtil.getPsiElementPopup(elements, _title).show(RelativePoint(mouseEvent))
	//		}
	//	},
	//	GutterIconRenderer.Alignment.RIGHT,
	//	{ _name }
	//)
}