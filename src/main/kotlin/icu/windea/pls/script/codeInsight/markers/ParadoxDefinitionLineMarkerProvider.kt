package icu.windea.pls.script.codeInsight.markers

import com.intellij.codeInsight.daemon.*
import com.intellij.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.navigation.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.model.constants.PlsStringConstants
import icu.windea.pls.script.psi.*

/**
 * 提供定义（definition）的装订线图标。
 */
class ParadoxDefinitionLineMarkerProvider : RelatedItemLineMarkerProvider() {
    override fun getName() = PlsBundle.message("script.gutterIcon.definition")

    override fun getIcon() = PlsIcons.Gutter.Definition

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        //何时显示装订线图标：element是definition
        if (element !is ParadoxScriptProperty) return
        val locationElement = element.propertyKey.idElement ?: return
        val definitionInfo = element.definitionInfo ?: return
        val icon = PlsIcons.Gutter.Definition
        val prefix = PlsStringConstants.definitionPrefix
        val name = definitionInfo.name
        val typeText = definitionInfo.typesText
        val tooltip = "<b>$prefix ${name.escapeXml().orAnonymous()}</b>: $typeText"
        val targets by lazy {
            val project = element.project
            val selector = selector(project, element).definition().contextSensitive()
            ParadoxDefinitionSearch.search(definitionInfo.name, definitionInfo.type, selector).findAll()
        }
        val lineMarkerInfo = createNavigationGutterIconBuilder(icon) { createGotoRelatedItem(targets) }
            .setTooltipText(tooltip)
            .setPopupTitle(PlsBundle.message("script.gutterIcon.definition.title"))
            .setTargets(NotNullLazyValue.lazy { targets })
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .setNamer { PlsBundle.message("script.gutterIcon.definition") }
            .createLineMarkerInfo(locationElement)
        //NavigateAction.setNavigateAction(
        //	lineMarkerInfo,
        //	PlsBundle.message("script.gutterIcon.definition.action"),
        //	PlsActions.GutterGotoDefinition
        //)
        result.add(lineMarkerInfo)
    }

    private fun createGotoRelatedItem(targets: Collection<ParadoxScriptDefinitionElement>): Collection<GotoRelatedItem> {
        return ParadoxGotoRelatedItem.createItems(targets, PlsBundle.message("script.gutterIcon.definition.group"))
    }
}
