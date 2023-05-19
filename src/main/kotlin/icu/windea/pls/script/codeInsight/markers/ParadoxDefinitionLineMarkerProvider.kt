package icu.windea.pls.script.codeInsight.markers

import com.intellij.codeInsight.daemon.*
import com.intellij.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.navigation.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.script.psi.*

/**
 * 定义（definition）的装订线图标提供器。
 */
class ParadoxDefinitionLineMarkerProvider : RelatedItemLineMarkerProvider() {
	override fun getName() = PlsBundle.message("script.gutterIcon.definition")
	
	override fun getIcon() = PlsIcons.Gutter.Definition
	
	override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
		//何时显示装订线图标：element是definition
		if(element !is ParadoxScriptProperty) return
		val locationElement = element.propertyKey.idElement ?: return
		val definitionInfo = element.definitionInfo ?: return
		val icon = PlsIcons.Gutter.Definition
		val tooltip = buildString {
			val name = definitionInfo.name
			val typeText = definitionInfo.typesText
			append(PlsBundle.message("prefix.definition")).append(" <b>").append(name.orAnonymous().escapeXml()).append("</b>: ").append(typeText)
		}
		val targets by lazy {
			val project = element.project
			val selector = definitionSelector(project, element).contextSensitive()
			ParadoxDefinitionSearch.search(definitionInfo.name, definitionInfo.type, selector).findAll()
		}
		val lineMarkerInfo = createNavigationGutterIconBuilder(icon) { createGotoRelatedItem(targets) }
			.setTooltipText(tooltip)
			.setPopupTitle(PlsBundle.message("script.gutterIcon.definition.title"))
			.setTargets(NotNullLazyValue.lazy { targets })
			.setAlignment(GutterIconRenderer.Alignment.RIGHT)
			.setNamer { PlsBundle.message("script.gutterIcon.definition") }
			.createLineMarkerInfo(locationElement)
		//NavigateAction.setNavigateAction(
		//	lineMarkerInfo,
		//	PlsBundle.message("script.gutterIcon.definition.action"),
		//	PlsActions.GutterGotoDefinition
		//)
		result.add(lineMarkerInfo)
	}
	
	private fun createGotoRelatedItem(targets: Collection<ParadoxScriptDefinitionElement>): Collection<GotoRelatedItem> {
		return ParadoxGotoRelatedItem.createItems(targets, PlsBundle.message("script.gutterIcon.definition.group"))
	}
}
