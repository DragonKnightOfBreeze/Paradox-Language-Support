package icu.windea.pls.lang.codeInsight.markers.script

import com.intellij.codeInsight.daemon.NavigateAction
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.orNull
import icu.windea.pls.core.orUnresolved
import icu.windea.pls.lang.actions.ChronicleActions
import icu.windea.pls.lang.codeInsight.markers.ParadoxLineMarkerProvider
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 提供封装变量（scriptedVariable）的装订线图标。
 */
class ParadoxScriptedVariableLineMarkerProvider : ParadoxLineMarkerProvider() {
    override fun getName() = ChronicleBundle.message("script.gutterIcon.scriptedVariable")

    override fun getIcon() = ChronicleIcons.Gutter.ScriptedVariable

    override fun getGroup() = ChronicleBundle.message("script.gutterIcon.scriptedVariable.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        if (element !is ParadoxScriptScriptedVariable) return
        val name = element.name?.orNull() ?: return

        ProgressManager.checkCanceled()

        val anchorElement = element.scriptedVariableName.idElement ?: return
        val project = element.project
        val targets = computeTargets(element, project, name)
        if (targets.isEmpty()) return // same-name scripted variables should exist
        val tooltip = computeTooltip(name)

        val title = ChronicleBundle.message("script.gutterIcon.scriptedVariable.title", name.orUnresolved())
        val findUsagesTitle = ChronicleBundle.message("script.gutterIcon.scriptedVariable.findUsagesTitle", name.orUnresolved())
        val navHandler = createNavigationHandler(title, findUsagesTitle, project) { targets }
        val lineMarkerInfo = createInfo(anchorElement, { tooltip }, navHandler, GutterIconRenderer.Alignment.LEFT) { targets }
        result.add(lineMarkerInfo)

        // bind navigation actions & show the corresponding intent actions in a separate group
        val actionText = ChronicleBundle.message("script.gutterIcon.scriptedVariable.action")
        NavigateAction.setNavigateAction(lineMarkerInfo, actionText, ChronicleActions.GotoScriptedVariables)
    }

    private fun computeTargets(element: ParadoxScriptScriptedVariable, project: Project, name: String): MutableSet<ParadoxScriptScriptedVariable> {
        val result = mutableSetOf<ParadoxScriptScriptedVariable>()
        val selector = ParadoxScriptedVariableSearch.selector(project, element).contextSensitive()
        // generally it only contains the current scripted variable itself
        ParadoxScriptedVariableSearch.searchLocal(name, selector).findAll().let { result.addAll(it) }
        // then search the global ones
        ParadoxScriptedVariableSearch.searchGlobal(name, selector).findAll().let { result.addAll(it) }
        return result
    }

    private fun computeTooltip(name: String): String {
        val prefix = ChronicleStrings.scriptedVariablePrefix
        return "$prefix <b>@${name.orUnresolved().escapeXml()}</b>"
    }
}
