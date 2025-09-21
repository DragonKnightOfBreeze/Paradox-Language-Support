package icu.windea.pls.lang.codeInsight.markers.localisation

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.codeInsight.navigation.NavigationGutterIconBuilderFacade
import icu.windea.pls.core.codeInsight.navigation.setTargets
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.codeInsight.markers.ParadoxRelatedItemLineMarkerProvider
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.ParadoxSyncedLocalisationSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.localisation
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxLocalisationType.Normal
import icu.windea.pls.model.ParadoxLocalisationType.Synced

/**
 * 提供本地化（localisation / localisation_synced）的装订线图标。
 *
 * 显示时机：当前 PSI 为 [ParadoxLocalisationProperty] 时显示。根据其类型：
 * - `Normal`：通过 [ParadoxLocalisationSearch] 搜索同名本地化；
 * - `Synced`：通过 [ParadoxSyncedLocalisationSearch] 搜索同名本地化。
 * 搜索时使用选择器 `selector(...).localisation().contextSensitive().preferLocale(...)`，
 * 其中 `preferLocale(locale)` 会优先选用指定语言环境，并兼容必要的回退策略。
 *
 * 导航目标：同名本地化属性集合（可能跨文件、跨模组）。图标落点为 `propertyKey.idElement`。
 */
class ParadoxLocalisationLineMarkerProvider : ParadoxRelatedItemLineMarkerProvider() {
    override fun getName() = PlsBundle.message("localisation.gutterIcon.localisation")

    override fun getIcon() = PlsIcons.Gutter.Localisation

    override fun getGroup() = PlsBundle.message("localisation.gutterIcon.localisation.group")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        // 何时显示装订线图标：element 是 localisation/localisation_synced
        if (element !is ParadoxLocalisationProperty) return
        val name = element.name.orNull()
        if (name == null) return
        val type = element.type
        if (type == null) return
        val icon = PlsIcons.Gutter.Localisation
        val tooltip = "($type) <b>$name</b>"
        val targets by lazy {
            val project = element.project
            val selector = selector(project, element).localisation().contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
            when (type) {
                Normal -> ParadoxLocalisationSearch.search(name, selector).findAll()
                Synced -> ParadoxSyncedLocalisationSearch.search(name, selector).findAll()
            }
        }
        val locationElement = element.propertyKey.idElement
        val lineMarkerInfo = NavigationGutterIconBuilderFacade.createForPsi(icon) { createGotoRelatedItem(targets) }
            .setTooltipText(tooltip)
            .setPopupTitle(PlsBundle.message("localisation.gutterIcon.localisation.title"))
            .setTargets { targets }
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .setNamer { PlsBundle.message("localisation.gutterIcon.localisation") }
            .createLineMarkerInfo(locationElement)
        result.add(lineMarkerInfo)

        // NavigateAction.setNavigateAction(
        //	lineMarkerInfo,
        //	PlsBundle.message("localisation.gutterIcon.localisation.action"),
        //	PlsActions.GutterGotoLocalisation
        // )
    }
}
