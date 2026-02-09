package icu.windea.pls.lang.codeInsight.markers.script

import com.intellij.codeInsight.daemon.NavigateAction
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.codeInsight.navigation.NavigationGutterIconBuilderFacade
import icu.windea.pls.core.codeInsight.navigation.setTargets
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.optimized
import icu.windea.pls.lang.actions.PlsActions
import icu.windea.pls.lang.codeInsight.markers.ParadoxRelatedItemLineMarkerProvider
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.search.ParadoxDefinitionInjectionSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.model.constants.PlsStrings
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 提供定义（definition）的相关注入（relatedDefinitionInjections，对应定义注入）的装订线图标。
 */
class ParadoxRelatedDefinitionInjectionsLineMarkerProvider : ParadoxRelatedItemLineMarkerProvider() {
    override fun getName() = PlsBundle.message("script.gutterIcon.relatedDefinitionInjections")

    override fun getIcon() = PlsIcons.Gutter.RelatedDefinitionInjections

    override fun getGroup() = PlsBundle.message("script.gutterIcon.relatedDefinitionInjections.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        // 何时显示装订线图标：element 是 definition，且存在对应的 definitionInjection
        if (element !is ParadoxScriptProperty) return
        val locationElement = element.propertyKey.idElement ?: return
        if (!ParadoxDefinitionInjectionManager.isSupported(selectGameType(element))) return // 忽略游戏类型不支持的情况
        val definitionInfo = element.definitionInfo ?: return
        if (!ParadoxDefinitionInjectionManager.canApply(definitionInfo)) return // 排除不期望匹配的定义
        // 显示在提示中 & 可导航
        val icon = PlsIcons.Gutter.RelatedDefinitionInjections
        val prefix = PlsStrings.definitionInjectionPrefix
        val tooltip = "$prefix <b>${definitionInfo.name.escapeXml()}</b>: ${definitionInfo.type}"
        val project = element.project
        val selector = selector(project, element).definitionInjection().contextSensitive()
        val targets0 = ParadoxDefinitionInjectionSearch.search(null, definitionInfo.name, definitionInfo.type, selector).findAll().mapNotNull { it.element }
        if (targets0.isEmpty()) return
        val targets = targets0.optimized()
        ProgressManager.checkCanceled()
        val lineMarkerInfo = NavigationGutterIconBuilderFacade.createForPsi(icon) { createGotoRelatedItem(targets) }
            .setTooltipText(tooltip)
            .setPopupTitle(PlsBundle.message("script.gutterIcon.relatedDefinitionInjections.title"))
            .setTargets { targets }
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .setNamer { PlsBundle.message("script.gutterIcon.relatedDefinitionInjections") }
            .createLineMarkerInfo(locationElement)
        result.add(lineMarkerInfo)

        // 绑定导航动作 & 在单独的分组中显示对应的意向动作
        NavigateAction.setNavigateAction(
            lineMarkerInfo,
            PlsBundle.message("script.gutterIcon.relatedDefinitionInjections.action"),
            PlsActions.GotoRelatedDefinitionInjections
        )
    }
}
