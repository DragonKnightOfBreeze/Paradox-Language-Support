package icu.windea.pls.script.codeInsight.makers

import com.intellij.codeInsight.daemon.*
import com.intellij.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

/**
 * 定义（definition）的装订线图标提供器。
 */
class ParadoxDefinitionLineMarkerProvider : RelatedItemLineMarkerProvider() {
	override fun getName() = PlsBundle.message("script.gutterIcon.definition")
	
	override fun getIcon() = PlsIcons.definitionGutterIcon
	
	override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
		//何时显示装订线图标：element是definition
		if(element !is ParadoxScriptProperty) return
		val definitionInfo = element.definitionInfo ?: return
		
		val icon = PlsIcons.definitionGutterIcon
		val tooltip = buildString {
			val name = definitionInfo.name
			val typeText = definitionInfo.typeText
			append(PlsDocBundle.message("name.script.definition")).append(" <b>").append(name.escapeXmlOrAnonymous()).append("</b>: ").append(typeText)
		}
		val project = element.project
		val targets = findDefinitionsByType(definitionInfo.name, definitionInfo.type, project)
		if(targets.isEmpty()) return
		val locationElement = element.propertyKey.let { it.propertyKeyId ?: it.quotedPropertyKeyId!! }
		val lineMarkerInfo = createNavigationGutterIconBuilder(icon){ createGotoRelatedItem(targets) }
			.setTooltipText(tooltip)
			.setPopupTitle(PlsBundle.message("script.gutterIcon.definition.title"))
			.setTargets(targets)
			.setAlignment(GutterIconRenderer.Alignment.RIGHT)
			.setNamer { PlsBundle.message("script.gutterIcon.definition") }
			.createLineMarkerInfo(locationElement)
		result.add(lineMarkerInfo)
	}
	
	private fun createGotoRelatedItem(targets:List<ParadoxDefinitionProperty>): Collection<GotoRelatedItem>{
		return GotoRelatedItem.createItems(targets, PlsBundle.message("script.gutterIcon.definition.group"))
	}
}