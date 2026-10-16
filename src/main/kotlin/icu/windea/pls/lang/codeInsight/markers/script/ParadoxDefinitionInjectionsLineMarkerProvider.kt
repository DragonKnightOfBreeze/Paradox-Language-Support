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
import icu.windea.pls.lang.definitionInjectionInfo
import icu.windea.pls.lang.search.ParadoxDefinitionInjectionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.model.ParadoxDefinitionInjectionInfo
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 提供定义注入（definitionInjection）的导航到同目标的所有定义注入的装订线图标。
 */
class ParadoxDefinitionInjectionsLineMarkerProvider : ParadoxLineMarkerProvider() {
    override fun getName() = ChronicleBundle.message("script.gutterIcon.definitionInjections")

    override fun getIcon() = ChronicleIcons.Gutter.DefinitionInjections

    override fun getGroup() = ChronicleBundle.message("script.gutterIcon.definitionInjections.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        if (element !is ParadoxScriptProperty) return
        if (!ParadoxDefinitionInjectionManager.isSupported(selectGameType(element))) return // skip the unsupported game types
        val info = element.definitionInjectionInfo ?: return
        if (!info.isTargetValid()) return // exclude the cases where the target or the target type is empty

        ProgressManager.checkCanceled()

        val anchorElement = element.propertyKey.idElement ?: return
        val project = info.project
        val targets by lazy { computeTargets(element, project, info) } // lazy computation
        val tooltip = computeTooltip(info)

        val title = ChronicleBundle.message("script.gutterIcon.definitionInjections.title", info.target.orEmpty())
        val findUsagesTitle = ChronicleBundle.message("script.gutterIcon.definitionInjections.findUsagesTitle", info.target.orEmpty())
        val navHandler = createNavigationHandler(title, findUsagesTitle, project) { targets }
        val lineMarkerInfo = createInfo(anchorElement, { tooltip }, navHandler, GutterIconRenderer.Alignment.LEFT) { targets }
        result.add(lineMarkerInfo)

        // bind navigation actions & show the corresponding intent actions in a separate group
        val actionText = ChronicleBundle.message("script.gutterIcon.definitionInjections.action")
        NavigateAction.setNavigateAction(lineMarkerInfo, actionText, ChronicleActions.GotoDefinitionInjections)
    }

    private fun computeTargets(element: ParadoxScriptProperty, project: Project, info: ParadoxDefinitionInjectionInfo): List<ParadoxScriptProperty> {
        val selector = ParadoxDefinitionInjectionSearch.selector(project, element).contextSensitive()
        return ParadoxDefinitionInjectionSearch.searchElement(null, info.target, info.type, selector).findAll()
    }

    private fun computeTooltip(info: ParadoxDefinitionInjectionInfo): String {
        val prefix = ChronicleStrings.definitionInjectionPrefix
        return "$prefix <b>${info.target.orEmpty().escapeXml()}</b>: ${info.typeText}"
    }
}
