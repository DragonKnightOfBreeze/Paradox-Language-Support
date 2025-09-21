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
import icu.windea.pls.core.util.anonymous
import icu.windea.pls.core.util.or
import icu.windea.pls.lang.actions.PlsActions
import icu.windea.pls.lang.codeInsight.markers.ParadoxRelatedItemLineMarkerProvider
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.model.constants.PlsStringConstants
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 提供定义（definition）的装订线图标。
 *
 * 显示时机：当前 PSI 为 [ParadoxScriptProperty]，且存在 `definitionInfo`。
 *
 * 目标收集：使用 `selector(project, element).definition().contextSensitive()`，
 * 通过 [ParadoxDefinitionSearch] 以 `definitionInfo.name` 与 `definitionInfo.type` 搜索同名定义集合。
 * 图标落点为 `propertyKey.idElement`。
 */
class ParadoxDefinitionLineMarkerProvider : ParadoxRelatedItemLineMarkerProvider() {
    override fun getName() = PlsBundle.message("script.gutterIcon.definition")

    override fun getIcon() = PlsIcons.Gutter.Definition

    override fun getGroup() = PlsBundle.message("script.gutterIcon.definition.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        // 何时显示装订线图标：element 是 definition
        if (element !is ParadoxScriptProperty) return
        val locationElement = element.propertyKey.idElement ?: return
        val definitionInfo = element.definitionInfo ?: return
        val icon = PlsIcons.Gutter.Definition
        val prefix = PlsStringConstants.definitionPrefix
        val name = definitionInfo.name
        val typeText = definitionInfo.typesText
        val tooltip = "<b>$prefix ${name.escapeXml().or.anonymous()}</b>: $typeText"
        val targets by lazy {
            val project = element.project
            val selector = selector(project, element).definition().contextSensitive()
            ParadoxDefinitionSearch.search(definitionInfo.name, definitionInfo.type, selector).findAll()
        }
        val lineMarkerInfo = NavigationGutterIconBuilderFacade.createForPsi(icon) { createGotoRelatedItem(targets) }
            .setTooltipText(tooltip)
            .setPopupTitle(PlsBundle.message("script.gutterIcon.definition.title"))
            .setTargets { targets }
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .setNamer { PlsBundle.message("script.gutterIcon.definition") }
            .createLineMarkerInfo(locationElement)
        result.add(lineMarkerInfo)

        // 绑定导航动作 & 在单独的分组中显示对应的意向动作
        NavigateAction.setNavigateAction(
        	lineMarkerInfo,
        	PlsBundle.message("script.gutterIcon.definition.action"),
        	PlsActions.GotoDefinitions
        )
    }
}
