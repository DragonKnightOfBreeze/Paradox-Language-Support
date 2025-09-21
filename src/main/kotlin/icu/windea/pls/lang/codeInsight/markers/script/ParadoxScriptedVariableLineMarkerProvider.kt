package icu.windea.pls.lang.codeInsight.markers.script

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.codeInsight.navigation.NavigationGutterIconBuilderFacade
import icu.windea.pls.core.codeInsight.navigation.setTargets
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.util.anonymous
import icu.windea.pls.core.util.or
import icu.windea.pls.lang.codeInsight.markers.ParadoxRelatedItemLineMarkerProvider
import icu.windea.pls.lang.search.ParadoxGlobalScriptedVariableSearch
import icu.windea.pls.lang.search.ParadoxLocalScriptedVariableSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.scriptedVariable
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.model.constants.PlsStringConstants
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 提供封装变量（scripted_variable）的装订线图标。
 */
class ParadoxScriptedVariableLineMarkerProvider : ParadoxRelatedItemLineMarkerProvider() {
    override fun getName() = PlsBundle.message("script.gutterIcon.scriptedVariable")

    override fun getIcon() = PlsIcons.Gutter.ScriptedVariable

    override fun getGroup() = PlsBundle.message("script.gutterIcon.scriptedVariable.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        // 何时显示装订线图标：element是scriptedVariable
        if (element !is ParadoxScriptScriptedVariable) return
        val locationElement = element.scriptedVariableName.idElement ?: return
        val prefix = PlsStringConstants.scriptedVariablePrefix
        val name = element.name ?: return
        val icon = PlsIcons.Gutter.ScriptedVariable
        val tooltip = "$prefix <b>@${name.escapeXml().or.anonymous()}</b>"
        val targets by lazy {
            val project = element.project
            val selector = selector(project, element).scriptedVariable().contextSensitive()
            val targets = mutableSetOf<ParadoxScriptScriptedVariable>()
            // 这里一般来说只会带上当前封装变量自身
            ParadoxLocalScriptedVariableSearch.search(name, selector).findAll().let { targets.addAll(it) }
            // 查找全局的
            ParadoxGlobalScriptedVariableSearch.search(name, selector).findAll().let { targets.addAll(it) }
            targets
        }
        val lineMarkerInfo = NavigationGutterIconBuilderFacade.createForPsi(icon) { createGotoRelatedItem(targets) }
            .setTooltipText(tooltip)
            .setPopupTitle(PlsBundle.message("script.gutterIcon.scriptedVariable.title"))
            .setTargets { targets }
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .setNamer { PlsBundle.message("script.gutterIcon.scriptedVariable") }
            .createLineMarkerInfo(locationElement)
        result.add(lineMarkerInfo)

        // NavigateAction.setNavigateAction(
        //	lineMarkerInfo,
        //	PlsBundle.message("script.gutterIcon.scriptedVariable.action"),
        //	PlsActions.GutterGotoScriptedVariable
        // )
    }
}
