package icu.windea.pls.lang.codeInsight.markers.localisation

import com.intellij.codeInsight.daemon.NavigateAction
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.actions.ChronicleActions
import icu.windea.pls.lang.codeInsight.markers.ParadoxLineMarkerProvider
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType

/**
 * 提供本地化（localisation 或 localisation_synced）的装订线图标。
 */
class ParadoxLocalisationLineMarkerProvider : ParadoxLineMarkerProvider() {
    override fun getName() = ChronicleBundle.message("localisation.gutterIcon.localisation")

    override fun getIcon() = ChronicleIcons.Gutter.Localisation

    override fun getGroup() = ChronicleBundle.message("localisation.gutterIcon.localisation.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        if (element !is ParadoxLocalisationProperty) return
        val name = element.name.orNull()
        if (name == null) return // name should not be null or empty
        val type = element.type
        if (type == null) return // type should not be null

        ProgressManager.checkCanceled()

        val anchorElement = element.propertyKey.idElement
        val project = element.project
        val targets by lazy { computeTargets(element, project, name, type) } // lazy computation
        val tooltip = computeTooltip(name, type)

        val title = ChronicleBundle.message("localisation.gutterIcon.localisation.title", name)
        val findUsagesTitle = ChronicleBundle.message("localisation.gutterIcon.localisation.findUsagesTitle", name)
        val navHandler = createNavigationHandler(title, findUsagesTitle, project) { targets }
        val lineMarkerInfo = createInfo(anchorElement, { tooltip }, navHandler, GutterIconRenderer.Alignment.LEFT) { targets }
        result.add(lineMarkerInfo)

        // bind navigation actions & show the corresponding intent actions in a separate group
        val actionText = ChronicleBundle.message("localisation.gutterIcon.localisation.action")
        NavigateAction.setNavigateAction(lineMarkerInfo, actionText, ChronicleActions.GotoLocalisations)
    }

    private fun computeTargets(element: ParadoxLocalisationProperty, project: Project, name: String, type: ParadoxLocalisationType): List<ParadoxLocalisationProperty> {
        val preferredLocale = ParadoxLocaleManager.getPreferredLocaleConfig()
        val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive().preferLocale(preferredLocale)
        return ParadoxLocalisationSearch.search(name, selector, type).findAll()
    }

    private fun computeTooltip(name: String, type: ParadoxLocalisationType): String {
        return "($type) <b>$name</b>"
    }
}
