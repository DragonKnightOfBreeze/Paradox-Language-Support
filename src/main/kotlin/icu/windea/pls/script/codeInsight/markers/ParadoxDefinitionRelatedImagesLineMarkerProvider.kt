package icu.windea.pls.script.codeInsight.markers

import com.intellij.codeInsight.daemon.*
import com.intellij.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.navigation.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*

/**
 * 提供定义的相关图片（relatedImages，对应类型为sprite的定义或者DDS图片）的装订线图标。
 */
class ParadoxDefinitionRelatedImagesLineMarkerProvider : RelatedItemLineMarkerProvider() {
    override fun getName() = PlsBundle.message("script.gutterIcon.relatedImages")

    override fun getIcon() = PlsIcons.Gutter.RelatedImages

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        //何时显示装订线图标：element是definition，且definitionInfo.images不为空，且计算得到的keys不为空
        if (element !is ParadoxScriptProperty) return
        val locationElement = element.propertyKey.idElement ?: return
        val definitionInfo = element.definitionInfo ?: return
        val imageInfos = definitionInfo.images
        if (imageInfos.isEmpty()) return
        //显示在提示中 & 可导航：去重后的一组DDS文件的filePath，或者sprite的definitionKey，不包括没有对应的图片的项，按解析顺序排序
        val icon = PlsIcons.Gutter.RelatedImages
        val prefix = PlsStringConstants.relatedImagePrefix
        val tooltipLines = mutableSetOf<String>()
        val keys = mutableSetOf<String>()
        val targets = mutableSetOf<PsiElement>() //这里需要考虑基于引用相等去重
        for ((key, locationExpression) in imageInfos) {
            ProgressManager.checkCanceled()
            val resolveResult = CwtLocationExpressionManager.resolve(locationExpression, element, definitionInfo) ?: continue
            if (resolveResult.elements.isNotEmpty()) {
                targets.addAll(resolveResult.elements)
            }
            if (resolveResult.message != null) {
                tooltipLines.add("$prefix $key = ${resolveResult.message}")
            } else if (resolveResult.elements.isNotEmpty() && keys.add(key)) {
                tooltipLines.add("$prefix $key = ${resolveResult.nameOrFilePath}")
            }
        }
        if (keys.isEmpty()) return
        if (targets.isEmpty()) return
        ProgressManager.checkCanceled()
        val lineMarkerInfo = createNavigationGutterIconBuilder(icon) { createGotoRelatedItem(targets) }
            .setTooltipText(tooltipLines.joinToString("<br>"))
            .setPopupTitle(PlsBundle.message("script.gutterIcon.relatedImages.title"))
            .setTargets(NotNullLazyValue.lazy { targets })
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .setNamer { PlsBundle.message("script.gutterIcon.relatedImages") }
            .createLineMarkerInfo(locationElement)
        //NavigateAction.setNavigateAction(
        //	lineMarkerInfo,
        //	PlsBundle.message("script.gutterIcon.relatedImages.action"),
        //	PlsActions.GutterGotoRelatedImage
        //)
        result.add(lineMarkerInfo)
    }

    private fun createGotoRelatedItem(targets: Set<PsiElement>): Collection<GotoRelatedItem> {
        return ParadoxGotoRelatedItem.createItems(targets, PlsBundle.message("script.gutterIcon.relatedImages.group"))
    }
}
