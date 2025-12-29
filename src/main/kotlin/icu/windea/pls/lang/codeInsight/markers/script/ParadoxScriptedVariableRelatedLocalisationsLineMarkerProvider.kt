package icu.windea.pls.lang.codeInsight.markers.script

import com.intellij.codeInsight.daemon.NavigateAction
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.codeInsight.navigation.NavigationGutterIconBuilderFacade
import icu.windea.pls.core.codeInsight.navigation.setTargets
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.actions.PlsActions
import icu.windea.pls.lang.codeInsight.markers.ParadoxRelatedItemLineMarkerProvider
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.model.constants.PlsStringConstants
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 提供封装变量（scripted_variable）的相关本地化（relatedLocalisation，对应 localisation）的装订线图标。
 */
class ParadoxScriptedVariableRelatedLocalisationsLineMarkerProvider : ParadoxRelatedItemLineMarkerProvider() {
    override fun getName() = PlsBundle.message("script.gutterIcon.scriptedVariableRelatedLocalisations")

    override fun getIcon() = PlsIcons.Gutter.RelatedLocalisations

    override fun getGroup() = PlsBundle.message("script.gutterIcon.scriptedVariableRelatedLocalisations.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        // 何时显示装订线图标：element 是 scriptedVariable，且存在同名本地化
        if (element !is ParadoxScriptScriptedVariable) return
        val locationElement = element.scriptedVariableName.idElement ?: return
        val name = element.name?.orNull() ?: return
        // 查找同名本地化（优先首选语言）
        val locale = ParadoxLocaleManager.getPreferredLocaleConfig()
        val targets0 = ParadoxScriptedVariableManager.getNameLocalisations(name, element, locale)
        if (targets0.isEmpty()) return
        val targets = targets0.optimized()
        // 提示文本：relatedLocalisation: <key>
        val prefix = PlsStringConstants.relatedLocalisationPrefix
        val tooltip = "$prefix $name"
        // 目标：单个本地化属性
        ProgressManager.checkCanceled()
        val icon = PlsIcons.Gutter.RelatedLocalisations
        val lineMarkerInfo = NavigationGutterIconBuilderFacade.createForPsi(icon) { createGotoRelatedItem(targets) }
            .setTooltipText(tooltip)
            .setPopupTitle(PlsBundle.message("script.gutterIcon.scriptedVariableRelatedLocalisations.title"))
            .setTargets { targets }
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .setNamer { PlsBundle.message("script.gutterIcon.scriptedVariableRelatedLocalisations") }
            .createLineMarkerInfo(locationElement)
        result.add(lineMarkerInfo)

        // 绑定导航动作 & 在单独的分组中显示对应的意向动作
        NavigateAction.setNavigateAction(
        	lineMarkerInfo,
        	PlsBundle.message("script.gutterIcon.scriptedVariableRelatedLocalisations.action"),
        	PlsActions.GotoRelatedLocalisations
        )
    }
}
