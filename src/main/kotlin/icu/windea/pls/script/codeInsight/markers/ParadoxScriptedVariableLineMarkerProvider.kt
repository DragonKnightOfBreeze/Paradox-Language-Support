package icu.windea.pls.script.codeInsight.markers

import com.intellij.codeInsight.daemon.*
import com.intellij.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.navigation.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.model.constants.*
import icu.windea.pls.script.psi.*

/**
 * 提供封装变量（scripted_variable）的装订线图标。
 */
class ParadoxScriptedVariableLineMarkerProvider : RelatedItemLineMarkerProvider() {
    override fun getName() = PlsBundle.message("script.gutterIcon.scriptedVariable")

    override fun getIcon() = PlsIcons.Gutter.ScriptedVariable

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        //何时显示装订线图标：element是scriptedVariable
        if (element !is ParadoxScriptScriptedVariable) return
        val locationElement = element.scriptedVariableName.idElement ?: return
        val prefix = PlsStringConstants.scriptedVariablePrefix
        val name = element.name ?: return
        val icon = PlsIcons.Gutter.ScriptedVariable
        val tooltip = "$prefix <b>@${name.escapeXml().orAnonymous()}</b>"
        val targets by lazy {
            val project = element.project
            val selector = selector(project, element).scriptedVariable().contextSensitive()
            val targets = mutableSetOf<ParadoxScriptScriptedVariable>()
            //这里一般来说只会带上当前封装变量自身
            ParadoxLocalScriptedVariableSearch.search(name, selector).findAll().let { targets.addAll(it) }
            //查找全局的
            ParadoxGlobalScriptedVariableSearch.search(name, selector).findAll().let { targets.addAll(it) }
            targets
        }
        val lineMarkerInfo = createNavigationGutterIconBuilder(icon) { createGotoRelatedItem(targets) }
            .setTooltipText(tooltip)
            .setPopupTitle(PlsBundle.message("script.gutterIcon.scriptedVariable.title"))
            .setTargets(NotNullLazyValue.lazy { targets })
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .setNamer { PlsBundle.message("script.gutterIcon.scriptedVariable") }
            .createLineMarkerInfo(locationElement)
        //NavigateAction.setNavigateAction(
        //	lineMarkerInfo,
        //	PlsBundle.message("script.gutterIcon.scriptedVariable.action"),
        //	PlsActions.GutterGotoScriptedVariable
        //)
        result.add(lineMarkerInfo)
    }

    private fun createGotoRelatedItem(targets: Collection<ParadoxScriptScriptedVariable>): Collection<GotoRelatedItem> {
        return ParadoxGotoRelatedItem.createItems(targets, PlsBundle.message("script.gutterIcon.scriptedVariable.group"))
    }
}
