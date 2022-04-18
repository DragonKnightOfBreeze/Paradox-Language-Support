package icu.windea.pls.script.codeInsight

import com.intellij.codeInsight.daemon.*
import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

class ParadoxDefinitionLineMarkerProvider : RelatedItemLineMarkerProvider() {
	companion object {
		private val _name = PlsBundle.message("script.gutterIcon.definition")
		private val _title = PlsBundle.message("script.gutterIcon.definition.title")
	}
	
	override fun getName() = _name
	
	override fun getIcon() = definitionGutterIcon
	
	override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
		//如果是definition，则添加definition的gutterIcon
		if(element is ParadoxScriptProperty) {
			val definitionInfo = element.definitionInfo ?: return
			val lineMarkerInfo = createMarker(definitionInfo, element)
			result.add(lineMarkerInfo)
		}
	}
	
	private fun createMarker(definitionInfo: ParadoxDefinitionInfo, element: ParadoxScriptProperty): RelatedItemLineMarkerInfo<PsiElement> {
		val icon = definitionGutterIcon
		val tooltip = buildString {
			val name = definitionInfo.name
			val typeText = definitionInfo.typeText
			append("(definition) <b>").append(name.escapeXmlOrAnonymous()).append("</b>: ").append(typeText)
		}
		val project = element.project
		val targets = findDefinitionsByType(definitionInfo.name, definitionInfo.type, project)
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