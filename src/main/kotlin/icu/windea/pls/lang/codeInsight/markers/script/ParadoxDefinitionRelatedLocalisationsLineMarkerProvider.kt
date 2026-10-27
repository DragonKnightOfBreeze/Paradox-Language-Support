package icu.windea.pls.lang.codeInsight.markers.script

import com.intellij.codeInsight.daemon.NavigateAction
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.lang.actions.ChronicleActions
import icu.windea.pls.lang.codeInsight.markers.ParadoxLineMarkerProvider
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.resolve.ParadoxLocalisationLocationExpressionResolveResult
import icu.windea.pls.lang.resolve.ParadoxLocationExpressionService
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.ParadoxDefinitionSource
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 提供定义（definition）的相关本地化（relatedLocalisations，对应本地化）的装订线图标。
 */
class ParadoxDefinitionRelatedLocalisationsLineMarkerProvider : ParadoxLineMarkerProvider() {
    override fun getName() = ChronicleBundle.message("script.gutterIcon.definitionRelatedLocalisations")

    override fun getIcon() = ChronicleIcons.Gutter.RelatedLocalisations

    override fun getGroup() = ChronicleBundle.message("script.gutterIcon.definitionRelatedLocalisations.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        if (element !is ParadoxScriptProperty) return
        val definitionInfo = element.definitionInfo ?: return

        // ignore inline definitions and injected definitions
        if (definitionInfo.source == ParadoxDefinitionSource.Inline || definitionInfo.source == ParadoxDefinitionSource.Injection) return
        // ignore definitions that without related localisation infos
        if (definitionInfo.localisations.isEmpty()) return

        ProgressManager.checkCanceled()

        val anchorElement = element.propertyKey.idElement ?: return
        val project = definitionInfo.project
        val resolvedElements = mutableSetOf<ParadoxLocalisationProperty>()
        val tooltipLines = mutableSetOf<String>()
        compute(element, definitionInfo, resolvedElements, tooltipLines)
        if (resolvedElements.isEmpty()) return // resolved localisations should exist
        val targets = resolvedElements
        val tooltip = tooltipLines.joinToString("<br>")

        val title = ChronicleBundle.message("script.gutterIcon.definitionRelatedLocalisations.title", definitionInfo.name)
        val findUsagesTitle = ChronicleBundle.message("script.gutterIcon.definitionRelatedLocalisations.findUsagesTitle", definitionInfo.name)
        val navHandler = createNavigationHandler(title, findUsagesTitle, project) { targets }
        val lineMarkerInfo = createInfo(anchorElement, { tooltip }, navHandler, GutterIconRenderer.Alignment.LEFT) { targets }
        result.add(lineMarkerInfo)

        // bind navigation actions & show the corresponding intent actions in a separate group
        val actionText = ChronicleBundle.message("script.gutterIcon.definitionRelatedLocalisations.action")
        NavigateAction.setNavigateAction(lineMarkerInfo, actionText, ChronicleActions.GotoRelatedLocalisations)
    }

    private fun compute(element: ParadoxScriptProperty, definitionInfo: ParadoxDefinitionInfo, resolved: MutableSet<ParadoxLocalisationProperty>, tooltipLines: MutableSet<String>) {
        val preferredLocale = ParadoxLocaleManager.getPreferredLocaleConfig()
        val prefix = ChronicleStrings.relatedLocalisationPrefix
        val localisationInfos = definitionInfo.localisations
        val toDistinct = HashSet<String>(localisationInfos.size)
        for ((key, locationExpression) in localisationInfos) {
            ProgressManager.checkCanceled()
            val resolveResult = ParadoxLocationExpressionService.resolve(locationExpression, element, definitionInfo) { preferLocale(preferredLocale) } ?: continue
            resolved.addAll(resolveResult.elements)
            if (!toDistinct.add(key)) break
            when (resolveResult) {
                is ParadoxLocalisationLocationExpressionResolveResult.Static -> {
                    if (resolveResult.elements.isEmpty()) continue
                    tooltipLines.add("$prefix $key = ${resolveResult.name}")
                }
                is ParadoxLocalisationLocationExpressionResolveResult.Dynamic -> {
                    tooltipLines.add("$prefix $key = ${resolveResult.message}")
                }
            }
        }
    }
}
