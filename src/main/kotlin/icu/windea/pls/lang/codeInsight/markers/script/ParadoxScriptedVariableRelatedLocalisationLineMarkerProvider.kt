package icu.windea.pls.lang.codeInsight.markers.script

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
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.ParadoxScriptedVariableManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.constants.PlsStringConstants
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 提供封装变量（scripted_variable）的相关本地化（relatedLocalisation，对应 localisation，不对应 localisation_synced）的装订线图标。
 *
 * 显示时机：当 [PsiElement] 为 [ParadoxScriptScriptedVariable]，且存在同名本地化时。
 *
 * 查找逻辑：通过 [ParadoxScriptedVariableManager.getNameLocalisations] 使用首选语言环境查找同名本地化。
 * 其中所用选择器会应用 `preferLocale(preferredLocale)` 策略：即优先使用该语言环境，并在必要时进行回退。
 */
class ParadoxScriptedVariableRelatedLocalisationLineMarkerProvider : ParadoxRelatedItemLineMarkerProvider() {
    override fun getName() = PlsBundle.message("script.gutterIcon.relatedLocalisations")

    override fun getIcon() = PlsIcons.Gutter.RelatedLocalisations

    override fun getGroup() = PlsBundle.message("script.gutterIcon.relatedLocalisations.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        // 何时显示装订线图标：element 是 scriptedVariable，且存在同名本地化
        if (element !is ParadoxScriptScriptedVariable) return
        val locationElement = element.scriptedVariableName.idElement ?: return
        val name = element.name?.orNull() ?: return
        // 查找同名本地化（优先首选语言）
        val locale = ParadoxLocaleManager.getPreferredLocaleConfig()
        val localisations = ParadoxScriptedVariableManager.getNameLocalisations(name, element, locale)
        if (localisations.isEmpty()) return
        // 提示文本：relatedLocalisation: <key>
        val prefix = PlsStringConstants.relatedLocalisationPrefix
        val tooltip = "$prefix $name"
        // 目标：单个本地化属性
        val targets = mutableSetOf<ParadoxLocalisationProperty>()
        targets.addAll(localisations)
        ProgressManager.checkCanceled()
        val icon = PlsIcons.Gutter.RelatedLocalisations
        val lineMarkerInfo = NavigationGutterIconBuilderFacade.createForPsi(icon) { createGotoRelatedItem(targets) }
            .setTooltipText(tooltip)
            .setPopupTitle(PlsBundle.message("script.gutterIcon.relatedLocalisations.title"))
            .setTargets { targets }
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .setNamer { PlsBundle.message("script.gutterIcon.relatedLocalisations") }
            .createLineMarkerInfo(locationElement)
        result.add(lineMarkerInfo)

        // NavigateAction.setNavigateAction(
        // 	lineMarkerInfo,
        // 	PlsBundle.message("script.gutterIcon.relatedLocalisations.action"),
        // 	PlsActions.GotoRelatedLocalisations
        // )
    }
}
