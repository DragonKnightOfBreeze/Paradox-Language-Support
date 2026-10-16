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
import icu.windea.pls.lang.resolve.ParadoxInlineScriptService
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 提供内联脚本用法（inlineScriptUsage）的导航到对应的内联脚本（inlineScript）的装订线图标。
 */
class ParadoxInlineScriptsLineMarkerProvider : ParadoxLineMarkerProvider() {
    override fun getName() = ChronicleBundle.message("script.gutterIcon.inlineScripts")

    override fun getIcon() = ChronicleIcons.Gutter.InlineScripts

    override fun getGroup() = ChronicleBundle.message("script.gutterIcon.inlineScripts.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        if (element !is ParadoxScriptProperty) return
        if (!ParadoxInlineScriptManager.isSupported(selectGameType(element))) return // skip the unsupported game types
        val expression = ParadoxInlineScriptService.getInlineScriptExpressionFromUsageElement(element) ?: return

        ProgressManager.checkCanceled()

        val anchorElement = element.propertyKey.idElement ?: return
        val project = element.project
        val targets = computeTargets(element, project, expression)
        if (targets.isEmpty()) return // inline script files should exist
        val tooltip = computeTooltip(expression)

        val title = ChronicleBundle.message("script.gutterIcon.inlineScripts.title", expression)
        val findUsagesTitle = ChronicleBundle.message("script.gutterIcon.inlineScripts.findUsagesTitle", expression)
        val navHandler = createNavigationHandler(title, findUsagesTitle, project) { targets }
        val lineMarkerInfo = createInfo(anchorElement, { tooltip }, navHandler, GutterIconRenderer.Alignment.LEFT) { targets }
        result.add(lineMarkerInfo)

        // bind navigation actions & show the corresponding intent actions in a separate group
        val actionText = ChronicleBundle.message("script.gutterIcon.inlineScripts.action")
        NavigateAction.setNavigateAction(lineMarkerInfo, actionText, ChronicleActions.GotoInlineScripts)
    }

    private fun computeTargets(element: ParadoxScriptProperty, project: Project, expression: String): List<ParadoxScriptFile> {
        return ParadoxInlineScriptManager.getInlineScriptFiles(expression, project, element)
    }

    private fun computeTooltip(expression: String): String {
        val prefix = ChronicleStrings.inlineScriptPrefix
        return "$prefix <b>${expression.escapeXml()}"
    }
}
