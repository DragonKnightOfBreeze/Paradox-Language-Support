package icu.windea.pls.localisation.codeInsight.markers

import com.intellij.codeInsight.daemon.*
import com.intellij.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.navigation.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.PlsStringConstants
import icu.windea.pls.script.psi.*

/**
 * 提供本地化（localisation）的相关定义的装订线图标。
 */
class ParadoxLocalisationRelatedDefinitionsLineMarkerProvider : RelatedItemLineMarkerProvider() {
    override fun getName() = PlsBundle.message("localisation.gutterIcon.relatedDefinitions")

    override fun getIcon() = PlsIcons.Gutter.RelatedDefinitions

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        if (element !is ParadoxLocalisationProperty) return
        val name = element.name.orNull()
        if (name == null) return
        val category = element.category
        if (category != ParadoxLocalisationCategory.Localisation) return
        val icon = PlsIcons.Gutter.RelatedDefinitions
        val prefix = PlsStringConstants.relatedDefinitionPrefix
        val targets = ParadoxLocalisationManager.getRelatedDefinitions(element)
        if (targets.isEmpty()) return
        ProgressManager.checkCanceled()
        val tooltipLines = targets.mapNotNull { target ->
            target.definitionInfo?.let { "$prefix ${it.name}: ${it.typesText}" }
        }
        val locationElement = element.propertyKey.idElement
        val lineMarkerInfo = createNavigationGutterIconBuilder(icon) { createGotoRelatedItem(targets) }
            .setTooltipText(tooltipLines.joinToString("<br>"))
            .setPopupTitle(PlsBundle.message("localisation.gutterIcon.relatedDefinitions.title"))
            .setTargets(NotNullLazyValue.lazy { targets })
            .setAlignment(GutterIconRenderer.Alignment.RIGHT)
            .setNamer { PlsBundle.message("localisation.gutterIcon.relatedDefinitions") }
            .createLineMarkerInfo(locationElement)
        //NavigateAction.setNavigateAction(
        //	lineMarkerInfo,
        //	PlsBundle.message("localisation.gutterIcon.relatedDefinitions.action"),
        //	PlsActions.GutterGotoRelatedDefinitions
        //)
        result.add(lineMarkerInfo)
    }

    private fun createGotoRelatedItem(targets: Collection<ParadoxScriptDefinitionElement>): Collection<GotoRelatedItem> {
        return ParadoxGotoRelatedItem.createItems(targets, PlsBundle.message("localisation.gutterIcon.relatedDefinitions.group"))
    }

    // <= 3s for l_simple_chinese.yml of Stellaris if enabled
    override fun isEnabledByDefault() = true
}
