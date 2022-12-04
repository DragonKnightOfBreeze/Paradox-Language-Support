package icu.windea.pls.script.codeInsight.markers

import com.intellij.codeInsight.daemon.*
import com.intellij.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.psi.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.actions.*
import icu.windea.pls.core.navigation.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*
import org.jetbrains.kotlin.idea.base.resources.*

/**
 * 定义（definition）的装订线图标提供器。
 */
class ParadoxDefinitionLineMarkerProvider : RelatedItemLineMarkerProvider() {
	override fun getName() = PlsBundle.message("script.gutterIcon.definition")
	
	override fun getIcon() = PlsIcons.Gutter.Definition
	
	override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
		//何时显示装订线图标：element是definition
		if(element !is ParadoxScriptProperty) return
		val definitionInfo = element.definitionInfo ?: return
		
		val icon = PlsIcons.Gutter.Definition
		val tooltip = buildString {
			val name = definitionInfo.name
			val typeText = definitionInfo.typesText
			append(PlsDocBundle.message("name.script.definition")).append(" <b>").append(name.escapeXmlOrAnonymous()).append("</b>: ").append(typeText)
		}
		val project = element.project
		val selector = definitionSelector().gameType(definitionInfo.gameType).preferRootFrom(element)
		val targets = ParadoxDefinitionSearch.search(definitionInfo.name, definitionInfo.type, project, selector = selector).findAll()
		if(targets.isEmpty()) return
		val locationElement = element.propertyKey.let { it.propertyKeyId ?: it.quotedPropertyKeyId!! }
		val lineMarkerInfo = createNavigationGutterIconBuilder(icon) { createGotoRelatedItem(targets) }
			.setTooltipText(tooltip)
			.setPopupTitle(PlsBundle.message("script.gutterIcon.definition.title"))
			.setTargets(targets)
			.setAlignment(GutterIconRenderer.Alignment.RIGHT)
			.setNamer { PlsBundle.message("script.gutterIcon.definition") }
			.createLineMarkerInfo(locationElement)
		NavigateAction.setNavigateAction(
			lineMarkerInfo,
			KotlinBundle.message(PlsBundle.message("script.gutterIcon.definition.action")),
			PlsActions.GutterGotoDefinition
		)
		result.add(lineMarkerInfo)
	}
	
	private fun createGotoRelatedItem(targets: Collection<ParadoxDefinitionProperty>): Collection<GotoRelatedItem> {
		return ParadoxGotoRelatedItem.createItems(targets, PlsBundle.message("script.gutterIcon.definition.group"))
	}
}
