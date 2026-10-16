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
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.search.ParadoxDefinitionInjectionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxDefinitionSource
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 提供定义（definition）的相关注入（relatedInjections，对应定义注入）的装订线图标。
 */
class ParadoxDefinitionRelatedInjectionsLineMarkerProvider : ParadoxLineMarkerProvider() {
    override fun getName() = ChronicleBundle.message("script.gutterIcon.definitionRelatedInjections")

    override fun getIcon() = ChronicleIcons.Gutter.RelatedDefinitionInjections

    override fun getGroup() = ChronicleBundle.message("script.gutterIcon.definitionRelatedInjections.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        if (element !is ParadoxScriptProperty) return
        val definitionInfo = element.definitionInfo ?: return

        // ignore inline definitions and injected definitions
        if (definitionInfo.source == ParadoxDefinitionSource.Inline || definitionInfo.source == ParadoxDefinitionSource.Injection) return
        // skip unsupported game types
        if (!ParadoxDefinitionInjectionManager.isSupported(definitionInfo.gameType)) return
        // skip definitions that are not expected to match
        if (!ParadoxDefinitionInjectionManager.canApply(definitionInfo)) return

        ProgressManager.checkCanceled()

        val anchorElement = element.propertyKey.idElement ?: return
        val project = definitionInfo.project
        val targets = computeTargets(element, project, definitionInfo)
        if (targets.isEmpty()) return // related definition injections should exist
        val tooltip = computeTooltip(definitionInfo)

        val title = ChronicleBundle.message("script.gutterIcon.definitionRelatedInjections.title", definitionInfo.name)
        val findUsagesTitle = ChronicleBundle.message("script.gutterIcon.definitionRelatedInjections.findUsagesTitle", definitionInfo.name)
        val navHandler = createNavigationHandler(title, findUsagesTitle, project) { targets }
        val lineMarkerInfo = createInfo(anchorElement, { tooltip }, navHandler, GutterIconRenderer.Alignment.LEFT) { targets }
        result.add(lineMarkerInfo)

        // bind navigation actions & show the corresponding intent actions in a separate group
        val actionText = ChronicleBundle.message("script.gutterIcon.definitionRelatedInjections.action")
        NavigateAction.setNavigateAction(lineMarkerInfo, actionText, ChronicleActions.GotoRelatedDefinitionInjections)
    }

    private fun computeTargets(element: ParadoxScriptProperty, project: Project, definitionInfo: ParadoxDefinitionInfo): List<ParadoxScriptProperty> {
        val selector = ParadoxDefinitionInjectionSearch.selector(project, element).contextSensitive()
        return ParadoxDefinitionInjectionSearch.searchElement(null, definitionInfo.name, definitionInfo.type, selector).findAll()
    }

    private fun computeTooltip(definitionInfo: ParadoxDefinitionInfo): String {
        val prefix = ChronicleStrings.definitionInjectionPrefix
        return "$prefix <b>${definitionInfo.name.escapeXml()}</b>: ${definitionInfo.type}"
    }
}
