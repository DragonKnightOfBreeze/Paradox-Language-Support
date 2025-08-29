package icu.windea.pls.lang.codeInsight.markers.localisation

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.navigation.GotoRelatedItem
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.codeInsight.markers.ParadoxRelatedItemLineMarkerProvider
import icu.windea.pls.lang.navigation.ParadoxGotoRelatedItem
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
 * 提供本地化（localisation/localisation_synced）的装订线图标。
 */
class ParadoxLocalisationLineMarkerProvider : ParadoxRelatedItemLineMarkerProvider() {
    override fun getName() = PlsBundle.message("localisation.gutterIcon.localisation")

    override fun getIcon() = PlsIcons.Gutter.Localisation

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        //何时显示装订线图标：element是localisation/localisation_synced
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
        val lineMarkerInfo = createNavigationGutterIconBuilder(icon) { createGotoRelatedItem(targets) }
            .setTooltipText(tooltip)
            .setPopupTitle(PlsBundle.message("localisation.gutterIcon.localisation.title"))
            .setTargets(NotNullLazyValue.lazy { targets })
            .setAlignment(GutterIconRenderer.Alignment.LEFT)
            .setNamer { PlsBundle.message("localisation.gutterIcon.localisation") }
            .createLineMarkerInfo(locationElement)
        //NavigateAction.setNavigateAction(
        //	lineMarkerInfo,
        //	PlsBundle.message("localisation.gutterIcon.localisation.action"),
        //	PlsActions.GutterGotoLocalisation
        //)
        result.add(lineMarkerInfo)
    }

    private fun createGotoRelatedItem(targets: Collection<ParadoxLocalisationProperty>): Collection<GotoRelatedItem> {
        return ParadoxGotoRelatedItem.createItems(targets, PlsBundle.message("localisation.gutterIcon.localisation.group"))
    }
}
