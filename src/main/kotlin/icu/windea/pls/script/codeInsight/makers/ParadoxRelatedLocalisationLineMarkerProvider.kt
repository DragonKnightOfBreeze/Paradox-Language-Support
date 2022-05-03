package icu.windea.pls.script.codeInsight.makers

import com.intellij.codeInsight.daemon.*
import com.intellij.codeInsight.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.*

/**
 * 定义的相关本地化（relatedLocalisation，对应localisation，不对应localisation_synced）的装订线图标提供器。
 */
class ParadoxRelatedLocalisationLineMarkerProvider : RelatedItemLineMarkerProvider() {
	override fun getName() = PlsBundle.message("script.gutterIcon.relatedLocalisation")
	
	override fun getIcon() = PlsIcons.relatedLocalisationGutterIcon
	
	override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
		//何时显示装订线图标：element是definition，且definitionInfo.localisation不为空，且计算得到的keys不为空
		if(element !is ParadoxScriptProperty) return
		val definitionInfo = element.definitionInfo ?: return
		val localisationConfig = definitionInfo.localisation
		if(localisationConfig.isEmpty()) return
		
		//显示在提示中 & 可导航：去重后的一组本地化的键名，不包括没有对应的本地化的项，按解析顺序排序
		val icon = PlsIcons.relatedLocalisationGutterIcon
		val tooltipBuilder = StringBuilder()
		val project = element.project
		val keys = mutableSetOf<String>()
		val targets = mutableSetOf<ParadoxLocalisationProperty>() //这里需要考虑基于引用相等去重
		var isFirst = true
		for((key, locationExpression) in localisationConfig) {
			val (localisationKey, localisations) = locationExpression.resolveAll(definitionInfo.name, null, project)
			if(localisations.isNotEmpty()) targets.addAll(localisations)
			if(localisations.isNotEmpty() && keys.add(key)) {
				if(isFirst) isFirst = false else tooltipBuilder.appendBr()
				tooltipBuilder.append(PlsDocBundle.message("name.script.relatedLocalisation")).append(" ").append(key).append(" = ").append(localisationKey)
			}
		}
		if(keys.isEmpty()) return
		val locationElement = element.propertyKey.let { it.propertyKeyId ?: it.quotedPropertyKeyId!! }
		val tooltip = tooltipBuilder.toString()
		val lineMarkerInfo = NavigationGutterIconBuilder.create(icon)
			.setTooltipText(tooltip)
			.setPopupTitle(PlsBundle.message("script.gutterIcon.relatedLocalisation.title"))
			.setTargets(targets)
			.setAlignment(GutterIconRenderer.Alignment.RIGHT)
			.setNamer { PlsBundle.message("script.gutterIcon.relatedLocalisation") }
			.createLineMarkerInfo(locationElement)
		result.add(lineMarkerInfo)
	}
}