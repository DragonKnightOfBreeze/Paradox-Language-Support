package icu.windea.pls.script.codeInsight.makers

import com.intellij.codeInsight.daemon.*
import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

/**
 * 定义（definition）的装订线图标提供器。
 */
class ParadoxDefinitionLineMarkerProvider : RelatedItemLineMarkerProvider() {
	companion object {
		private val _name = PlsBundle.message("script.gutterIcon.definition")
		private val _title = PlsBundle.message("script.gutterIcon.definition.title")
	}
	
	override fun getName() = _name
	
	override fun getIcon() = PlsIcons.definitionGutterIcon
	
	override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
		//何时显示装订线图标：element是definition
		if(element !is ParadoxScriptProperty) return
		val definitionInfo = element.definitionInfo ?: return
		
		val icon = PlsIcons.definitionGutterIcon
		val tooltip = buildString {
			val name = definitionInfo.name
			val typeText = definitionInfo.typesText
			append("(definition) <b>").append(name.escapeXmlOrAnonymous()).append("</b>: ").append(typeText)
		}
		val project = element.project
		val targets = findDefinitionsByType(definitionInfo.name, definitionInfo.type, project)
		val locationElement = element.propertyKey.let { it.propertyKeyId ?: it.quotedPropertyKeyId!! }
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