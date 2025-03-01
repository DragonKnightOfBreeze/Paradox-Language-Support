package icu.windea.pls.localisation.codeInsight.markers

import com.intellij.codeInsight.daemon.*
import com.intellij.navigation.*
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.ParadoxLocalisationCategory.*
import icu.windea.pls.lang.navigation.ParadoxGotoRelatedItem.Companion as ParadoxGotoRelatedItem1

/**
 * 本地化（localisation/localisation_synced）的装订线图标提供器。
 */
class ParadoxLocalisationLineMarkerProvider : RelatedItemLineMarkerProvider() {
    override fun getName() = PlsBundle.message("localisation.gutterIcon.localisation")

    override fun getIcon() = PlsIcons.Gutter.Localisation

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<*>>) {
        //何时显示装订线图标：element是localisation/localisation_synced
        if (element !is ParadoxLocalisationProperty) return
        val locationElement = element.propertyKey.propertyKeyId
        val name = element.name
        val category = element.category ?: return
        val icon = PlsIcons.Gutter.Localisation
        val tooltip = buildString {
            append("$category <b>").append(name).append("</b>")
        }
        val targets by lazy {
            val project = element.project
            val selector = selector(project, element).localisation().contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
            when (category) {
                Localisation -> ParadoxLocalisationSearch.search(name, selector).findAll()
                SyncedLocalisation -> ParadoxSyncedLocalisationSearch.search(name, selector).findAll()
            }
        }
        val lineMarkerInfo = createNavigationGutterIconBuilder(icon) { createGotoRelatedItem(targets) }
            .setTooltipText(tooltip)
            .setPopupTitle(PlsBundle.message("localisation.gutterIcon.localisation.title"))
            .setTargets(NotNullLazyValue.lazy { targets })
            .setAlignment(GutterIconRenderer.Alignment.RIGHT)
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
        return ParadoxGotoRelatedItem1.createItems(targets, PlsBundle.message("localisation.gutterIcon.localisation.group"))
    }
}
