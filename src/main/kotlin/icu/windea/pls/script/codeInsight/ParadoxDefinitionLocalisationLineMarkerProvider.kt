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
				append("(definition localisation) ").append(n).append(" = ").append(kn)
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
			.setAlignment(GutterIconRenderer.Alignment.RIGHT)
			.setNamer { _name }
			.createLineMarkerInfo(targetElement)
	}
}