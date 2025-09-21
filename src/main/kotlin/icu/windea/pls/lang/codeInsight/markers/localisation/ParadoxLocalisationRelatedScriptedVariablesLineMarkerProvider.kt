package icu.windea.pls.lang.codeInsight.markers.localisation

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.codeInsight.navigation.NavigationGutterIconBuilderFacade
import icu.windea.pls.core.codeInsight.navigation.setTargets
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.codeInsight.markers.ParadoxRelatedItemLineMarkerProvider
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.model.constants.PlsStringConstants

/**
 * 提供本地化（localisation）的相关封装变量的装订线图标。
 *
 * 显示时机：当前 PSI 为 [ParadoxLocalisationProperty] 且类型为 [ParadoxLocalisationType.Normal] 时。
 */
class ParadoxLocalisationRelatedScriptedVariablesLineMarkerProvider : ParadoxRelatedItemLineMarkerProvider() {
    override fun getName() = PlsBundle.message("localisation.gutterIcon.relatedScriptedVariables")

    override fun getIcon() = PlsIcons.Gutter.RelatedScriptedVariables

    override fun getGroup() = PlsBundle.message("localisation.gutterIcon.relatedScriptedVariables.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        // 何时显示装订线图标：element 是 localisation，且存在相关的定义
        if (element !is ParadoxLocalisationProperty) return
        val locationElement = element.propertyKey.idElement
        val name = element.name.orNull() ?: return
        val type = element.type
        if (type != ParadoxLocalisationType.Normal) return
        // 目标：相关封装变量（即同名的封装变量）
        val targets = ParadoxLocalisationManager.getRelatedScriptedVariables(element)
        if (targets.isEmpty()) return
        ProgressManager.checkCanceled()
        val prefix = PlsStringConstants.relatedScriptedVariablePrefix
        val tooltip = "$prefix @${name}"
        val icon = PlsIcons.Gutter.RelatedScriptedVariables
        val lineMarkerInfo = NavigationGutterIconBuilderFacade.createForPsi(icon) { createGotoRelatedItem(targets) }
            .setTooltipText(tooltip)
            .setPopupTitle(PlsBundle.message("localisation.gutterIcon.relatedScriptedVariables.title"))
            .setTargets { targets }
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .setNamer { PlsBundle.message("localisation.gutterIcon.relatedScriptedVariables") }
            .createLineMarkerInfo(locationElement)
        result.add(lineMarkerInfo)

        // NavigateAction.setNavigateAction(
        //	lineMarkerInfo,
        //	PlsBundle.message("localisation.gutterIcon.relatedScriptedVariables.action"),
        //	PlsActions.GutterGotoRelatedScriptedVariables
        // )
    }
}
