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
import icu.windea.pls.lang.actions.ChronicleActions
import icu.windea.pls.lang.codeInsight.markers.ParadoxLineMarkerProvider
import icu.windea.pls.lang.defineVariableInfo
import icu.windea.pls.lang.search.ParadoxDefineVariableSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.model.ParadoxDefineVariableInfo
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 提供定值变量变量（defineVariable）的装订线图标。
 */
class ParadoxDefineVariableLineMarkerProvider : ParadoxLineMarkerProvider() {
    override fun getName() = ChronicleBundle.message("script.gutterIcon.defineVariable")

    override fun getIcon() = ChronicleIcons.Gutter.DefineVariable

    override fun getGroup() = ChronicleBundle.message("script.gutterIcon.defineVariable.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        if (element !is ParadoxScriptProperty) return
        val defineVariableInfo = element.defineVariableInfo ?: return
        val name = "${defineVariableInfo.namespace}.${defineVariableInfo.variable}"

        ProgressManager.checkCanceled()

        val anchorElement = element.propertyKey.idElement ?: return
        val project = defineVariableInfo.project
        val targets by lazy { computeTargets(element, project, defineVariableInfo) } // lazy computation
        val tooltip = computeTooltip(name)

        val title = ChronicleBundle.message("script.gutterIcon.defineVariable.title", name)
        val findUsagesTitle = ChronicleBundle.message("script.gutterIcon.defineVariable.findUsagesTitle", name)
        val navHandler = createNavigationHandler(title, findUsagesTitle, project) { targets }
        val lineMarkerInfo = createInfo(anchorElement, { tooltip }, navHandler, GutterIconRenderer.Alignment.LEFT) { targets }
        result.add(lineMarkerInfo)

        // bind navigation actions & show the corresponding intent actions in a separate group
        val actionText = ChronicleBundle.message("script.gutterIcon.defineVariable.action")
        NavigateAction.setNavigateAction(lineMarkerInfo, actionText, ChronicleActions.GotoDefineVariables)
    }

    private fun computeTargets(element: ParadoxScriptProperty, project: Project, defineVariableInfo: ParadoxDefineVariableInfo): List<ParadoxScriptProperty> {
        val selector = ParadoxDefineVariableSearch.selector(project, element).contextSensitive()
        return ParadoxDefineVariableSearch.search(defineVariableInfo.namespace, defineVariableInfo.variable, selector).findAll()
    }

    private fun computeTooltip(name: String): String {
        val prefix = ChronicleStrings.defineVariablePrefix
        return "$prefix <b>${name.escapeXml()}</b>"
    }
}
