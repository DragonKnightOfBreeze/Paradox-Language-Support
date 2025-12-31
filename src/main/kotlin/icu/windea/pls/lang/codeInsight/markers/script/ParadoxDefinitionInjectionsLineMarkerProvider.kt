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
import icu.windea.pls.lang.search.selector.definitionInjection
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.model.constants.PlsStringConstants
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 提供定义（definition）的导航到对应的定义注入（definitionInjection）的装订线图标。
 */
class ParadoxDefinitionInjectionsLineMarkerProvider : ParadoxRelatedItemLineMarkerProvider() {
    override fun getName() = PlsBundle.message("script.gutterIcon.definitionInjections")

    override fun getIcon() = PlsIcons.Gutter.DefinitionInjections

    override fun getGroup() = PlsBundle.message("script.gutterIcon.definitionInjections.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        // 何时显示装订线图标：element 是 definition，且存在对应的 definitionInjection
        if (element !is ParadoxScriptProperty) return
        val locationElement = element.propertyKey.idElement ?: return
        val definitionInfo = element.definitionInfo ?: return
        if (!ParadoxDefinitionInjectionManager.canApply(definitionInfo)) return // 排除不期望匹配的定义
        val icon = PlsIcons.Gutter.DefinitionInjections
        val prefix = PlsStringConstants.definitionInjectionPrefix
        val tooltip = "$prefix <b>${definitionInfo.name.escapeXml()}</b>: ${definitionInfo.type}"
        val targetKey = definitionInfo.type + "@" + definitionInfo.name
        val project = element.project
        val selector = selector(project, element).definitionInjection().contextSensitive()
        val targets0 = ParadoxDefinitionInjectionSearch.search(null, targetKey, selector).findAll()
        if (targets0.isEmpty()) return
        val targets = targets0.optimized()
        ProgressManager.checkCanceled()
        val lineMarkerInfo = NavigationGutterIconBuilderFacade.createForPsi(icon) { createGotoRelatedItem(targets) }
            .setTooltipText(tooltip)
            .setPopupTitle(PlsBundle.message("script.gutterIcon.definitionInjections.title"))
            .setTargets { targets }
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .setNamer { PlsBundle.message("script.gutterIcon.definitionInjections") }
            .createLineMarkerInfo(locationElement)
        result.add(lineMarkerInfo)

        // 绑定导航动作 & 在单独的分组中显示对应的意向动作
        NavigateAction.setNavigateAction(
            lineMarkerInfo,
            PlsBundle.message("script.gutterIcon.definitionInjections.action"),
            PlsActions.GotoDefinitionInjections
        )
    }
}
