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
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.util.CwtLocationExpressionManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.constants.PlsStringConstants
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 提供定义的相关本地化（relatedLocalisation，对应 localisation，不对应 localisation_synced）的装订线图标。
 *
 * 显示时机：当前 PSI 为 [ParadoxScriptProperty]，且其 `definitionInfo.localisations` 非空时。
 *
 * 解析逻辑：对 `definitionInfo.localisations` 的每一项 `(key, locationExpression)`，
 * 使用 [CwtLocationExpressionManager.resolve] 进行解析；解析时通过 `preferLocale(preferredLocale)`
 * 指定首选语言，该选择器会优先使用该语言环境，并按需要回退到其他可用结果。
 *
 * 目标与提示：
 * - 目标为解析出的 [ParadoxLocalisationProperty] 集合（基于引用去重）。
 * - 提示行规则：
 *   - 若有消息（`resolveResult.message`），显示 `relatedLocalisationPrefix key = message`；
 *   - 否则若存在元素且键名首次出现，显示 `relatedLocalisationPrefix key = name`。
 * - 图标落点：`propertyKey.idElement`。
 */
class ParadoxDefinitionRelatedLocalisationsLineMarkerProvider : ParadoxRelatedItemLineMarkerProvider() {
    override fun getName() = PlsBundle.message("script.gutterIcon.relatedLocalisations")

    override fun getIcon() = PlsIcons.Gutter.RelatedLocalisations

    override fun getGroup() = PlsBundle.message("script.gutterIcon.relatedLocalisations.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        // 何时显示装订线图标：element 是 definition，且 definitionInfo.localisation 不为空，且计算得到的 keys 不为空
        if (element !is ParadoxScriptProperty) return
        val locationElement = element.propertyKey.idElement ?: return
        val definitionInfo = element.definitionInfo ?: return
        val localisationInfos = definitionInfo.localisations
        if (localisationInfos.isEmpty()) return
        // 显示在提示中 & 可导航：去重后的一组本地化的键名，不包括没有对应的本地化的项，按解析顺序排序
        val icon = PlsIcons.Gutter.RelatedLocalisations
        val prefix = PlsStringConstants.relatedLocalisationPrefix
        val tooltipLines = mutableSetOf<String>()
        val keys = mutableSetOf<String>()
        val targets = mutableSetOf<ParadoxLocalisationProperty>() // 这里需要考虑基于引用相等去重
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
        val lineMarkerInfo = NavigationGutterIconBuilderFacade.createForPsi(icon) { createGotoRelatedItem(targets) }
            .setTooltipText(tooltipLines.joinToString("<br>"))
            .setPopupTitle(PlsBundle.message("script.gutterIcon.relatedLocalisations.title"))
            .setTargets { targets }
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .setNamer { PlsBundle.message("script.gutterIcon.relatedLocalisations") }
            .createLineMarkerInfo(locationElement)
        result.add(lineMarkerInfo)

        // NavigateAction.setNavigateAction(
        //	lineMarkerInfo,
        //	PlsBundle.message("script.gutterIcon.relatedLocalisation.action"),
        //	PlsActions.GutterGotoRelatedLocalisation
        // )
    }
}
