package icu.windea.pls.lang.codeInsight.markers.localisation

import com.intellij.codeInsight.daemon.NavigateAction
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.codeInsight.navigation.NavigationGutterIconBuilderFacade
import icu.windea.pls.core.codeInsight.navigation.setTargets
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.actions.PlsActions
import icu.windea.pls.lang.codeInsight.markers.ParadoxRelatedItemLineMarkerProvider
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.model.constants.PlsStringConstants

/**
 * 提供本地化（localisation）的相关定义的装订线图标。
 *
 * 显示时机：当前 PSI 为 [ParadoxLocalisationProperty] 且类型为 [ParadoxLocalisationType.Normal] 时。
 */
class ParadoxLocalisationRelatedDefinitionsLineMarkerProvider : ParadoxRelatedItemLineMarkerProvider() {
    override fun getName() = PlsBundle.message("localisation.gutterIcon.relatedDefinitions")

    override fun getIcon() = PlsIcons.Gutter.RelatedDefinitions

    override fun getGroup() = PlsBundle.message("localisation.gutterIcon.relatedDefinitions.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        // 何时显示装订线图标：element 是 localisation，且存在相关的定义
        if (element !is ParadoxLocalisationProperty) return
        val locationElement = element.propertyKey.idElement
        val name = element.name.orNull()
        if (name == null) return
        val type = element.type
        if (type != ParadoxLocalisationType.Normal) return
        // 目标：相关定义
        val targets = ParadoxLocalisationManager.getRelatedDefinitions(element)
        if (targets.isEmpty()) return
        ProgressManager.checkCanceled()
        val prefix = PlsStringConstants.relatedDefinitionPrefix
        val tooltipLines = targets.mapNotNull { target ->
            target.definitionInfo?.let { "$prefix ${it.name}: ${it.typesText}" }
        }
        val icon = PlsIcons.Gutter.RelatedDefinitions
        val lineMarkerInfo = NavigationGutterIconBuilderFacade.createForPsi(icon) { createGotoRelatedItem(targets) }
            .setTooltipText(tooltipLines.joinToString("<br>"))
            .setPopupTitle(PlsBundle.message("localisation.gutterIcon.relatedDefinitions.title"))
            .setTargets { targets }
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .setNamer { PlsBundle.message("localisation.gutterIcon.relatedDefinitions") }
            .createLineMarkerInfo(locationElement)
        result.add(lineMarkerInfo)

        // 绑定导航动作 & 在单独的分组中显示对应的意向动作
        NavigateAction.setNavigateAction(
        	lineMarkerInfo,
        	PlsBundle.message("localisation.gutterIcon.relatedDefinitions.action"),
        	PlsActions.GotoRelatedDefinitions
        )
    }

    // <= 3s for l_simple_chinese.yml of Stellaris if enabled, so it's ok
    // override fun isEnabledByDefault() = true
}
