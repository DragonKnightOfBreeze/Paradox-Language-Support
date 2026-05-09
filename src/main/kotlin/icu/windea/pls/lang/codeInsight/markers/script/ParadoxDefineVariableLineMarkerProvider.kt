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
import icu.windea.pls.lang.defineVariableInfo
import icu.windea.pls.lang.search.ParadoxDefineVariableSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.model.constants.PlsStrings
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 提供定值变量变量（defineVariable）的装订线图标。
 */
class ParadoxDefineVariableLineMarkerProvider : ParadoxRelatedItemLineMarkerProvider() {
    override fun getName() = PlsBundle.message("script.gutterIcon.defineVariable")

    override fun getIcon() = PlsIcons.Gutter.DefineVariable

    override fun getGroup() = PlsBundle.message("script.gutterIcon.defineVariable.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        // 何时显示装订线图标：element 是 defineVariable
        if (element !is ParadoxScriptProperty) return
        val locationElement = element.propertyKey.idElement ?: return
        val defineVariableInfo = element.defineVariableInfo ?: return

        ProgressManager.checkCanceled()
        val icon = PlsIcons.Gutter.DefineVariable
        val prefix = PlsStrings.defineVariablePrefix
        val tooltip = "$prefix <b>${defineVariableInfo.namespace.escapeXml()}.${defineVariableInfo.variable.escapeXml()}</b>"
        val targets by lazy {
            val project = element.project
            val selector = selector(project, element).define().contextSensitive()
            val targets0 = ParadoxDefineVariableSearch.search(defineVariableInfo.namespace, defineVariableInfo.variable, selector).findAll()
            targets0.optimized()
        }
        val lineMarkerInfo = NavigationGutterIconBuilderFacade.createForPsi(icon) { createGotoRelatedItem(targets) }
            .setTooltipText(tooltip)
            .setPopupTitle(PlsBundle.message("script.gutterIcon.defineVariable.title"))
            .setTargets { targets }
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .setNamer { PlsBundle.message("script.gutterIcon.defineVariable") }
            .createLineMarkerInfo(locationElement)
        result.add(lineMarkerInfo)

        // 绑定导航动作 & 在单独的分组中显示对应的意向动作
        val actionText = PlsBundle.message("script.gutterIcon.defineVariable.action")
        NavigateAction.setNavigateAction(lineMarkerInfo, actionText, PlsActions.GotoDefineVariables)
    }
}
