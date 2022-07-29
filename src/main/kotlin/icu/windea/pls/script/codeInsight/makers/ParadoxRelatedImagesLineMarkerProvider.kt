package icu.windea.pls.script.codeInsight.makers

import com.intellij.codeInsight.daemon.*
import com.intellij.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.navigation.*
import icu.windea.pls.script.psi.*

/**
 * 定义的相关图片（relatedImages，对应类型为sprite的定义或者DDS图片）的装订线图标提供器。
 */
class ParadoxRelatedImagesLineMarkerProvider : RelatedItemLineMarkerProvider() {
	override fun getName() = PlsBundle.message("script.gutterIcon.relatedImages")
	
	override fun getIcon() = PlsIcons.Gutter.RelatedImages
	
	override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
		//何时显示装订线图标：element是definition，且definitionInfo.images不为空，且计算得到的keys不为空
		if(element !is ParadoxScriptProperty) return
		val definitionInfo = element.definitionInfo ?: return
		val imagesConfig = definitionInfo.images
		if(imagesConfig.isEmpty()) return
		
		//显示在提示中 & 可导航：去重后的一组DDS文件的filePath，或者sprite的definitionKey，不包括没有对应的图片的项，按解析顺序排序
		val icon = PlsIcons.Gutter.RelatedImages
		val tooltipBuilder = StringBuilder()
		val project = element.project
		val keys = mutableSetOf<String>()
		val targets = mutableSetOf<PsiElement>() //这里需要考虑基于引用相等去重
		var isFirst = true
		for((key, locationExpression) in imagesConfig) {
			val (filePath, files) = locationExpression.resolveAll(definitionInfo.name, element, project) ?: continue
			if(files.isNotEmpty()) targets.addAll(files)
			if(files.isNotEmpty() && keys.add(key)) {
				if(isFirst) isFirst = false else tooltipBuilder.appendBr()
				tooltipBuilder.append(PlsDocBundle.message("name.script.relatedImage")).append(" ").append(key).append(" = ").append(filePath)
			}
		}
		if(keys.isEmpty()) return
		if(targets.isEmpty()) return
		val locationElement = element.propertyKey.let { it.propertyKeyId ?: it.quotedPropertyKeyId!! }
		val tooltip = tooltipBuilder.toString()
		val lineMarkerInfo = createNavigationGutterIconBuilder(icon) { createGotoRelatedItem(targets) }
			.setTooltipText(tooltip)
			.setPopupTitle(PlsBundle.message("script.gutterIcon.relatedImages.title"))
			.setTargets(targets)
			.setAlignment(GutterIconRenderer.Alignment.RIGHT)
			.setNamer { PlsBundle.message("script.gutterIcon.relatedImages") }
			.createLineMarkerInfo(locationElement)
		result.add(lineMarkerInfo)
	}
	
	private fun createGotoRelatedItem(targets: Set<PsiElement>): Collection<GotoRelatedItem> {
		return ParadoxGotoRelatedItem.createItems(targets, PlsBundle.message("script.gutterIcon.relatedImages.group"))
	}
}