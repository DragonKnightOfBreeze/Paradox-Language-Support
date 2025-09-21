package icu.windea.pls.lang.codeInsight.markers.script

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.codeInsight.navigation.NavigationGutterIconBuilderFacade
import icu.windea.pls.core.codeInsight.navigation.setTargets
import icu.windea.pls.lang.codeInsight.markers.ParadoxRelatedItemLineMarkerProvider
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.util.CwtLocationExpressionManager
import icu.windea.pls.model.constants.PlsStringConstants
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 提供定义的相关图片（relatedImages，对应定义或者图片）的装订线图标。
 *
 * 显示时机：当前 PSI 为 [ParadoxScriptProperty]，且其 `definitionInfo.images` 非空时。
 *
 * 解析逻辑：遍历 `definitionInfo.images` 中的 `(key, locationExpression)`，调用
 * [CwtLocationExpressionManager.resolve] 获取解析结果：
 * - 若返回元素非空，则加入导航目标集合；
 * - 若存在消息，提示行使用 `relatedImagePrefix key = message`；
 * - 否则在首次出现该键名时，提示行使用 `relatedImagePrefix key = nameOrFilePath`。
 */
class ParadoxDefinitionRelatedImagesLineMarkerProvider : ParadoxRelatedItemLineMarkerProvider() {
    override fun getName() = PlsBundle.message("script.gutterIcon.relatedImages")

    override fun getIcon() = PlsIcons.Gutter.RelatedImages

    override fun getGroup() = PlsBundle.message("script.gutterIcon.relatedImages.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        // 何时显示装订线图标：element 是 definition，且 definitionInfo.images 不为空，且计算得到的 keys 不为空
        if (element !is ParadoxScriptProperty) return
        val locationElement = element.propertyKey.idElement ?: return
        val definitionInfo = element.definitionInfo ?: return
        val imageInfos = definitionInfo.images
        if (imageInfos.isEmpty()) return
        // 显示在提示中 & 可导航：文件路径或者定义名，不包括没有对应的图片的项，按解析顺序排序
        val icon = PlsIcons.Gutter.RelatedImages
        val prefix = PlsStringConstants.relatedImagePrefix
        val tooltipLines = mutableSetOf<String>()
        val keys = mutableSetOf<String>()
        val targets = mutableSetOf<PsiElement>() // 这里需要考虑基于引用相等去重
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
        val lineMarkerInfo = NavigationGutterIconBuilderFacade.createForPsi(icon) { createGotoRelatedItem(targets) }
            .setTooltipText(tooltipLines.joinToString("<br>"))
            .setPopupTitle(PlsBundle.message("script.gutterIcon.relatedImages.title"))
            .setTargets { targets }
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .setNamer { PlsBundle.message("script.gutterIcon.relatedImages") }
            .createLineMarkerInfo(locationElement)
        result.add(lineMarkerInfo)

        // NavigateAction.setNavigateAction(
        //	lineMarkerInfo,
        //	PlsBundle.message("script.gutterIcon.relatedImages.action"),
        //	PlsActions.GutterGotoRelatedImage
        // )
    }
}
