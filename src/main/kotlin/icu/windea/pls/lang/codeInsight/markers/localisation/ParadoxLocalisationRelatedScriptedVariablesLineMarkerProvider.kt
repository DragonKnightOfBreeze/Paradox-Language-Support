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
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 提供本地化（localisation）的相关封装变量（relatedScriptedVariables）的装订线图标。
 */
class ParadoxLocalisationRelatedScriptedVariablesLineMarkerProvider : ParadoxLineMarkerProvider() {
    override fun getName() = ChronicleBundle.message("localisation.gutterIcon.relatedScriptedVariables")

    override fun getIcon() = ChronicleIcons.Gutter.RelatedScriptedVariables

    override fun getGroup() = ChronicleBundle.message("localisation.gutterIcon.relatedScriptedVariables.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        if (element !is ParadoxLocalisationProperty) return
        val name = element.name.orNull()
        if (name == null) return // name should not be null or empty
        val type = element.type
        if (type != ParadoxLocalisationType.Normal) return // type should be `Normal`

        ProgressManager.checkCanceled()

        val anchorElement = element.propertyKey.idElement
        val project = element.project
        val targets = computeTargets(element)
        if (targets.isEmpty()) return // related scripted variables should exist
        val tooltip = computeTooltip(name)

        val title = ChronicleBundle.message("localisation.gutterIcon.relatedScriptedVariables.title", name)
        val findUsagesTitle = ChronicleBundle.message("localisation.gutterIcon.relatedScriptedVariables.findUsagesTitle", name)
        val navHandler = createNavigationHandler(title, findUsagesTitle, project) { targets }
        val lineMarkerInfo = createInfo(anchorElement, { tooltip }, navHandler, GutterIconRenderer.Alignment.LEFT) { targets }
        result.add(lineMarkerInfo)

        // bind navigation actions & show the corresponding intent actions in a separate group
        val actionText = ChronicleBundle.message("localisation.gutterIcon.relatedScriptedVariables.action")
        NavigateAction.setNavigateAction(lineMarkerInfo, actionText, ChronicleActions.GotoRelatedScriptedVariables)
    }

    private fun computeTargets(element: ParadoxLocalisationProperty): List<ParadoxScriptScriptedVariable> {
        return ParadoxLocalisationManager.getRelatedScriptedVariables(element)
    }

    private fun computeTooltip(name: String): String {
        val prefix = ChronicleStrings.relatedScriptedVariablePrefix
        return "$prefix @$name"
    }
}
