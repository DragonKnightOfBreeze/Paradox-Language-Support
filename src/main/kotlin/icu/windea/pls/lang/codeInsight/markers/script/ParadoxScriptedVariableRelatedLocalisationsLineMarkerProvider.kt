package icu.windea.pls.lang.codeInsight.markers.script

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
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 提供封装变量（scriptedVariable）的相关本地化（relatedLocalisation，对应 localisation）的装订线图标。
 */
class ParadoxScriptedVariableRelatedLocalisationsLineMarkerProvider : ParadoxLineMarkerProvider() {
    override fun getName() = ChronicleBundle.message("script.gutterIcon.scriptedVariableRelatedLocalisations")

    override fun getIcon() = ChronicleIcons.Gutter.RelatedLocalisations

    override fun getGroup() = ChronicleBundle.message("script.gutterIcon.scriptedVariableRelatedLocalisations.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        if (element !is ParadoxScriptScriptedVariable) return
        val name = element.name?.orNull() ?: return

        ProgressManager.checkCanceled()

        val anchorElement = element.scriptedVariableName.idElement ?: return
        val project = element.project
        val targets = computeTargets(element)
        if (targets.isEmpty()) return // same-name localisations should exist
        val tooltip = computeTooltip(name)

        val title = ChronicleBundle.message("script.gutterIcon.scriptedVariableRelatedLocalisations.title", name)
        val findUsagesTitle = ChronicleBundle.message("script.gutterIcon.scriptedVariableRelatedLocalisations.findUsagesTitle", name)
        val navHandler = createNavigationHandler(title, findUsagesTitle, project) { targets }
        val lineMarkerInfo = createInfo(anchorElement, { tooltip }, navHandler, GutterIconRenderer.Alignment.LEFT) { targets }
        result.add(lineMarkerInfo)

        // bind navigation actions & show the corresponding intent actions in a separate group
        val actionText = ChronicleBundle.message("script.gutterIcon.scriptedVariableRelatedLocalisations.action")
        NavigateAction.setNavigateAction(lineMarkerInfo, actionText, ChronicleActions.GotoRelatedLocalisations)
    }

    private fun computeTargets(element: ParadoxScriptScriptedVariable): List<ParadoxLocalisationProperty> {
        val locale = ParadoxLocaleManager.getPreferredLocaleConfig()
        return ParadoxScriptedVariableManager.getRelatedLocalisations(element, locale)
    }

    private fun computeTooltip(name: String): String {
        val prefix = ChronicleStrings.relatedLocalisationPrefix
        return "$prefix $name"
    }
}
