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
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*

/**
 * 提供定义的相关本地化（relatedLocalisation，对应localisation，不对应localisation_synced）的装订线图标。
 */
class ParadoxDefinitionRelatedLocalisationsLineMarkerProvider : RelatedItemLineMarkerProvider() {
    override fun getName() = PlsBundle.message("script.gutterIcon.relatedLocalisations")

    override fun getIcon() = PlsIcons.Gutter.RelatedLocalisations

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        //何时显示装订线图标：element是definition，且definitionInfo.localisation不为空，且计算得到的keys不为空
        if (element !is ParadoxScriptProperty) return
        val locationElement = element.propertyKey.idElement ?: return
        val definitionInfo = element.definitionInfo ?: return
        val localisationInfos = definitionInfo.localisations
        if (localisationInfos.isEmpty()) return
        //显示在提示中 & 可导航：去重后的一组本地化的键名，不包括没有对应的本地化的项，按解析顺序排序
        val icon = PlsIcons.Gutter.RelatedLocalisations
        val prefix = PlsStringConstants.relatedLocalisationPrefix
        val tooltipLines = mutableSetOf<String>()
        val keys = mutableSetOf<String>()
        val targets = mutableSetOf<ParadoxLocalisationProperty>() //这里需要考虑基于引用相等去重
        val project = element.project
        val preferredLocale = ParadoxLocaleManager.getPreferredLocaleConfig()
        for ((key, locationExpression) in localisationInfos) {
            ProgressManager.checkCanceled()
            val resolveResult = CwtLocationExpressionManager.resolve(locationExpression, element, definitionInfo) { preferLocale(preferredLocale) } ?: continue
            if (resolveResult.elements.isNotEmpty()) {
                targets.addAll(resolveResult.elements)
            }
            if (resolveResult.message != null) {
                tooltipLines.add("$prefix $key = ${resolveResult.message}")
            } else if (resolveResult.elements.isNotEmpty() && keys.add(key)) {
                tooltipLines.add("$prefix $key = ${resolveResult.name}")
            }
        }
        if (keys.isEmpty()) return
        if (targets.isEmpty()) return
        ProgressManager.checkCanceled()
        val lineMarkerInfo = createNavigationGutterIconBuilder(icon) { createGotoRelatedItem(targets) }
            .setTooltipText(tooltipLines.joinToString("<br>"))
            .setPopupTitle(PlsBundle.message("script.gutterIcon.relatedLocalisations.title"))
            .setTargets(NotNullLazyValue.lazy { targets })
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .setNamer { PlsBundle.message("script.gutterIcon.relatedLocalisations") }
            .createLineMarkerInfo(locationElement)
        //NavigateAction.setNavigateAction(
        //	lineMarkerInfo,
        //	PlsBundle.message("script.gutterIcon.relatedLocalisation.action"),
        //	PlsActions.GutterGotoRelatedLocalisation
        //)
        result.add(lineMarkerInfo)
    }

    private fun createGotoRelatedItem(targets: Set<ParadoxLocalisationProperty>): Collection<GotoRelatedItem> {
        return ParadoxGotoRelatedItem.createItems(targets, PlsBundle.message("script.gutterIcon.relatedLocalisations.group"))
    }
}
