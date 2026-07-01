package icu.windea.pls.lang.codeInsight.markers.script

import com.intellij.codeInsight.daemon.NavigateAction
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.core.codeInsight.navigation.NavigationGutterIconBuilderFacade
import icu.windea.pls.core.codeInsight.navigation.setTargets
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.optimized
import icu.windea.pls.lang.actions.ChronicleActions
import icu.windea.pls.lang.codeInsight.markers.ParadoxRelatedItemLineMarkerProvider
import icu.windea.pls.lang.resolve.ParadoxInlineScriptService
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.model.constants.ChronicleStrings
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 提供内联脚本用法（inlineScriptUsage）的导航到对应的内联脚本（inlineScript）的装订线图标。
 */
class ParadoxInlineScriptsLineMarkerProvider : ParadoxRelatedItemLineMarkerProvider() {
    override fun getName() = ChronicleBundle.message("script.gutterIcon.inlineScripts")

    override fun getIcon() = ChronicleIcons.Gutter.InlineScripts

    override fun getGroup() = ChronicleBundle.message("script.gutterIcon.inlineScripts.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        // 何时显示装订线图标：element 是 inlineScriptUsage
        if (element !is ParadoxScriptProperty) return
        val locationElement = element.propertyKey.idElement ?: return
        if (!ParadoxInlineScriptManager.isSupported(selectGameType(element))) return // 忽略游戏类型不支持的情况
        val expression = ParadoxInlineScriptService.getInlineScriptExpressionFromUsageElement(element) ?: return

        ProgressManager.checkCanceled()
        val icon = ChronicleIcons.Gutter.InlineScripts
        val prefix = ChronicleStrings.inlineScriptPrefix
        val tooltip = "$prefix <b>${expression.escapeXml()}"
        val targets by lazy {
            val targets0 = ParadoxInlineScriptManager.getInlineScriptFiles(expression, element.project, element)
            targets0.optimized()
        }
        val lineMarkerInfo = NavigationGutterIconBuilderFacade.createForPsi(icon) { createGotoRelatedItem(targets) }
            .setTooltipText(tooltip)
            .setPopupTitle(ChronicleBundle.message("script.gutterIcon.inlineScripts.title"))
            .setTargets { targets }
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .setNamer { ChronicleBundle.message("script.gutterIcon.inlineScripts") }
            .createLineMarkerInfo(locationElement)
        result.add(lineMarkerInfo)

        // 绑定导航动作 & 在单独的分组中显示对应的意向动作
        val actionText = ChronicleBundle.message("script.gutterIcon.inlineScripts.action")
        NavigateAction.setNavigateAction(lineMarkerInfo, actionText, ChronicleActions.GotoInlineScripts)
    }
}
