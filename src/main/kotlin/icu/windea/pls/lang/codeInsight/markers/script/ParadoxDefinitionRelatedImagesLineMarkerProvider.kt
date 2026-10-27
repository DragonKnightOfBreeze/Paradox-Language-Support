package icu.windea.pls.lang.codeInsight.markers.script

import com.intellij.codeInsight.daemon.NavigateAction
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.lang.actions.ChronicleActions
import icu.windea.pls.lang.codeInsight.markers.ParadoxLineMarkerProvider
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.resolve.ParadoxImageLocationExpressionResolveResult
import icu.windea.pls.lang.resolve.ParadoxLocationExpressionService
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxDefinitionSource
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 提供定义（definition）的相关图片（relatedImages，对应定义或图片）的装订线图标。
 */
class ParadoxDefinitionRelatedImagesLineMarkerProvider : ParadoxLineMarkerProvider() {
    override fun getName() = ChronicleBundle.message("script.gutterIcon.definitionRelatedImages")

    override fun getIcon() = ChronicleIcons.Gutter.RelatedImages

    override fun getGroup() = ChronicleBundle.message("script.gutterIcon.definitionRelatedImages.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        if (element !is ParadoxScriptProperty) return
        val definitionInfo = element.definitionInfo ?: return

        // ignore inline definitions and injected definitions
        if (definitionInfo.source == ParadoxDefinitionSource.Inline || definitionInfo.source == ParadoxDefinitionSource.Injection) return
        // ignore definitions that without related image infos
        if (definitionInfo.images.isEmpty()) return

        ProgressManager.checkCanceled()

        val anchorElement = element.propertyKey.idElement ?: return
        val project = definitionInfo.project
        val resolvedElements = mutableSetOf<PsiElement>()
        val tooltipLines = mutableSetOf<String>()
        compute(element, definitionInfo, resolvedElements, tooltipLines)
        if (resolvedElements.isEmpty()) return // resolved images should exist
        val targets = resolvedElements
        val tooltip = tooltipLines.joinToString("<br>")

        val title = ChronicleBundle.message("script.gutterIcon.definitionRelatedImages.title", definitionInfo.name)
        val findUsagesTitle = ChronicleBundle.message("script.gutterIcon.definitionRelatedImages.findUsagesTitle", definitionInfo.name)
        val navHandler = createNavigationHandler(title, findUsagesTitle, project) { targets }
        val lineMarkerInfo = createInfo(anchorElement, { tooltip }, navHandler, GutterIconRenderer.Alignment.LEFT) { targets }
        result.add(lineMarkerInfo)

        // bind navigation actions & show the corresponding intent actions in a separate group
        val actionText = ChronicleBundle.message("script.gutterIcon.definitionRelatedImages.action")
        NavigateAction.setNavigateAction(lineMarkerInfo, actionText, ChronicleActions.GotoRelatedImages)
    }

    private fun compute(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo, resolved: MutableSet<PsiElement>, tooltipLines: MutableSet<String>): MutableSet<PsiElement> {
        val prefix = ChronicleStrings.relatedImagePrefix
        val imageInfos = definitionInfo.images
        val toDistinct = HashSet<String>(imageInfos.size)
        for ((key, locationExpression) in imageInfos) {
            ProgressManager.checkCanceled()
            val resolveResult = ParadoxLocationExpressionService.resolve(locationExpression, element, definitionInfo) ?: continue
            resolved.addAll(resolveResult.elements)
            if (!toDistinct.add(key)) break
            when (resolveResult) {
                is ParadoxImageLocationExpressionResolveResult.Static -> {
                    if (resolveResult.elements.isEmpty()) continue
                    tooltipLines.add("$prefix $key = ${resolveResult.name}")
                }
                is ParadoxImageLocationExpressionResolveResult.Dynamic -> {
                    tooltipLines.add("$prefix $key = ${resolveResult.message}")
                }
            }
        }
        return resolved
    }
}
