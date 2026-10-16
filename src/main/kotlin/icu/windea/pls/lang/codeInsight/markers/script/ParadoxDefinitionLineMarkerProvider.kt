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
import icu.windea.pls.core.orAnonymous
import icu.windea.pls.lang.actions.ChronicleActions
import icu.windea.pls.lang.codeInsight.markers.ParadoxLineMarkerProvider
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxDefinitionSource
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 提供定义（definition）的装订线图标。
 */
class ParadoxDefinitionLineMarkerProvider : ParadoxLineMarkerProvider() {
    override fun getName() = ChronicleBundle.message("script.gutterIcon.definition")

    override fun getIcon() = ChronicleIcons.Gutter.Definition

    override fun getGroup() = ChronicleBundle.message("script.gutterIcon.definition.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        if (element !is ParadoxScriptProperty) return
        val definitionInfo = element.definitionInfo ?: return

        // ignore inline definitions and injected definitions
        if (definitionInfo.source == ParadoxDefinitionSource.Inline || definitionInfo.source == ParadoxDefinitionSource.Injection) return

        ProgressManager.checkCanceled()

        val anchorElement = element.propertyKey.idElement ?: return
        val project = definitionInfo.project
        val targets by lazy { computeTargets(element, project, definitionInfo) } // lazy computation
        val tooltip = computeTooltip(definitionInfo)

        val title = ChronicleBundle.message("script.gutterIcon.definition.title", definitionInfo.name.orAnonymous())
        val findUsagesTitle = ChronicleBundle.message("script.gutterIcon.definition.findUsagesTitle", definitionInfo.name.orAnonymous())
        val navHandler = createNavigationHandler(title, findUsagesTitle, project) { targets }
        val lineMarkerInfo = createInfo(anchorElement, { tooltip }, navHandler, GutterIconRenderer.Alignment.LEFT) { targets }
        result.add(lineMarkerInfo)

        // bind navigation actions & show the corresponding intent actions in a separate group
        val actionText = ChronicleBundle.message("script.gutterIcon.definition.action")
        NavigateAction.setNavigateAction(lineMarkerInfo, actionText, ChronicleActions.GotoDefinitions)
    }

    private fun computeTargets(element: ParadoxScriptProperty, project: Project, definitionInfo: ParadoxDefinitionInfo): List<ParadoxDefinitionElement> {
        val selector = ParadoxDefinitionSearch.selector(project, element).contextSensitive()
        return ParadoxDefinitionSearch.searchElement(definitionInfo.name, definitionInfo.type, selector).findAll()
    }

    private fun computeTooltip(definitionInfo: ParadoxDefinitionInfo): String {
        val prefix = ChronicleStrings.definitionPrefix
        return "$prefix <b>${definitionInfo.name.orAnonymous().escapeXml()}</b>: ${definitionInfo.typeText}"
    }
}
