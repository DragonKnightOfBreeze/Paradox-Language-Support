package icu.windea.pls.lang.codeInsight.markers.localisation

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.navigation.GotoRelatedItem
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.codeInsight.markers.ParadoxRelatedItemLineMarkerProvider
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.navigation.ParadoxGotoRelatedItem
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.model.constants.PlsStringConstants
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement

/**
 * 提供本地化（localisation）的相关定义的装订线图标。
 */
class ParadoxLocalisationRelatedDefinitionsLineMarkerProvider : ParadoxRelatedItemLineMarkerProvider() {
    override fun getName() = PlsBundle.message("localisation.gutterIcon.relatedDefinitions")

    override fun getIcon() = PlsIcons.Gutter.RelatedDefinitions

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        if (element !is ParadoxLocalisationProperty) return
        val name = element.name.orNull()
        if (name == null) return
        val type = element.type
        if (type != ParadoxLocalisationType.Normal) return
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
