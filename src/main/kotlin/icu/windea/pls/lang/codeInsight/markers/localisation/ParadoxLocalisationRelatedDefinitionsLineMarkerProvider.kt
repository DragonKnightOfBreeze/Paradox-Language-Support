package icu.windea.pls.lang.codeInsight.markers.localisation

import com.intellij.codeInsight.daemon.NavigateAction
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.actions.ChronicleActions
import icu.windea.pls.lang.codeInsight.markers.ParadoxLineMarkerProvider
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.model.constants.ChronicleStrings

/**
 * 提供本地化（localisation）的相关定义的装订线图标。
 */
class ParadoxLocalisationRelatedDefinitionsLineMarkerProvider : ParadoxLineMarkerProvider() {
    override fun getName() = ChronicleBundle.message("localisation.gutterIcon.relatedDefinitions")

    override fun getIcon() = ChronicleIcons.Gutter.RelatedDefinitions

    override fun getGroup() = ChronicleBundle.message("localisation.gutterIcon.relatedDefinitions.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        if (element !is ParadoxLocalisationProperty) return
        val name = element.name.orNull()
        if (name == null) return // name should not be null or empty
        if (element.type != ParadoxLocalisationType.Normal) return // type should be `Normal`

        ProgressManager.checkCanceled()

        val anchorElement = element.propertyKey.idElement
        val project = element.project
        val targets = computeTargets(element)
        if (targets.isEmpty()) return // related definitions should exist
        val tooltip = computeTooltip(targets)

        val title = ChronicleBundle.message("localisation.gutterIcon.relatedDefinitions.title", name)
        val findUsagesTitle = ChronicleBundle.message("localisation.gutterIcon.relatedDefinitions.findUsagesTitle", name)
        val navHandler = createNavigationHandler(title, findUsagesTitle, project) { targets }
        val lineMarkerInfo = createInfo(anchorElement, { tooltip }, navHandler, GutterIconRenderer.Alignment.LEFT) { targets }
        result.add(lineMarkerInfo)

        // bind navigation actions & show the corresponding intent actions in a separate group
        val actionText = ChronicleBundle.message("localisation.gutterIcon.relatedDefinitions.action")
        NavigateAction.setNavigateAction(lineMarkerInfo, actionText, ChronicleActions.GotoRelatedDefinitions)
    }

    private fun computeTargets(element: ParadoxLocalisationProperty): List<ParadoxDefinitionElement> {
        return ParadoxLocalisationManager.getRelatedDefinitions(element)
    }

    private fun computeTooltip(targets: List<ParadoxDefinitionElement>): String {
        val prefix = ChronicleStrings.relatedDefinitionPrefix
        return targets.mapNotNull { target ->
            target.definitionInfo?.let { "$prefix ${it.name}: ${it.typeText}" }
        }.joinToString("<br>")
    }

    // <= 3s for l_simple_chinese.yml of Stellaris if enabled, so it's ok
    // override fun isEnabledByDefault() = true
}
