package icu.windea.pls.lang.codeInsight.markers.script

import com.intellij.codeInsight.daemon.NavigateAction
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.codeInsight.navigation.NavigationGutterIconBuilderFacade
import icu.windea.pls.core.codeInsight.navigation.setTargets
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.optimized
import icu.windea.pls.lang.actions.PlsActions
import icu.windea.pls.lang.codeInsight.markers.ParadoxRelatedItemLineMarkerProvider
import icu.windea.pls.lang.search.ParadoxDefinitionInjectionSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.definitionInjection
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.model.constants.PlsStrings
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 提供定义注入（definitionInjection）的导航到同目标的所有定义注入的装订线图标。
 */
class ParadoxDefinitionInjectionsLineMarkerProvider : ParadoxRelatedItemLineMarkerProvider() {
    override fun getName() = PlsBundle.message("script.gutterIcon.definitionInjections")

    override fun getIcon() = PlsIcons.Gutter.DefinitionInjections

    override fun getGroup() = PlsBundle.message("script.gutterIcon.definitionInjections.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        // 何时显示装订线图标：element 是 definitionInjection
        if (element !is ParadoxScriptProperty) return
        val locationElement = element.propertyKey.idElement ?: return
        if (!ParadoxDefinitionInjectionManager.isSupported(selectGameType(element))) return // 忽略游戏类型不支持的情况
        val info = ParadoxDefinitionInjectionManager.getInfo(element) ?: return
        if (info.target.isNullOrEmpty()) return // 排除目标为空的情况
        if (info.type.isNullOrEmpty()) return // 排除目标定义的类型为空的情况
        val icon = PlsIcons.Gutter.DefinitionInjections
        val prefix = PlsStrings.definitionInjectionPrefix
        val tooltip = "$prefix <b>${info.target.escapeXml()}</b>: ${info.type}"
        val targets by lazy {
            val targetKey = info.type + "@" + info.target
            val project = element.project
            val selector = selector(project, element).definitionInjection().contextSensitive()
            val targets0 = ParadoxDefinitionInjectionSearch.search(null, targetKey, selector).findAll()
            targets0.optimized()
        }
        val lineMarkerInfo = NavigationGutterIconBuilderFacade.createForPsi(icon) { createGotoRelatedItem(targets) }
            .setTooltipText(tooltip)
            .setPopupTitle(PlsBundle.message("script.gutterIcon.definitionInjections.title"))
            .setTargets { targets }
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .setNamer { PlsBundle.message("script.gutterIcon.definitionInjections") }
            .createLineMarkerInfo(locationElement)
        result.add(lineMarkerInfo)

        // 绑定导航动作 & 在单独的分组中显示对应的意向动作
        NavigateAction.setNavigateAction(
            lineMarkerInfo,
            PlsBundle.message("script.gutterIcon.definitionInjections.action"),
            PlsActions.GotoDefinitionInjections
        )
    }
}
