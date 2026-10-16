package icu.windea.pls.lang.codeInsight.markers

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.navigation.GotoRelatedItem
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsContexts
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.core.optimized
import javax.swing.Icon

abstract class ParadoxLineMarkerProvider : RelatedItemLineMarkerProvider() {
    abstract override fun getName(): String

    abstract fun getGroup(): @NlsContexts.PopupTitle String

    protected fun createInfo(
        element: PsiElement,
        range: TextRange,
        icon: Icon?,
        tooltipProvider: () -> String,
        navHandler: GutterIconNavigationHandler<PsiElement>,
        alignment: GutterIconRenderer.Alignment,
        targetsProvider: () -> Collection<GotoRelatedItem>,
        accessibleNameProvider: () -> String,
    ): ParadoxLineMarkerInfo<PsiElement> {
        return ParadoxLineMarkerInfo(element, range, icon, tooltipProvider, navHandler, alignment, targetsProvider, accessibleNameProvider)
    }

    protected fun createInfo(
        element: PsiElement,
        tooltipProvider: () -> String,
        navHandler: GutterIconNavigationHandler<PsiElement>,
        alignment: GutterIconRenderer.Alignment,
        targetsProvider: () -> Collection<PsiElement>,
    ): ParadoxLineMarkerInfo<PsiElement> {
        return createInfo(element, element.textRange, this.icon, tooltipProvider, navHandler, alignment, { createItems(targetsProvider()) }, { this.name })
    }

    protected fun createNavigationHandler(
        title: @NlsContexts.PopupTitle String,
        findUsagesTitle: String,
        project: Project,
        targetsProvider: () -> Collection<PsiElement>,
    ): ParadoxLineMarkerNavigationHandler<PsiElement> {
        return ParadoxLineMarkerNavigationHandler(title, findUsagesTitle, project, targetsProvider)
    }

    private fun createItems(elements: Collection<PsiElement>): Collection<ParadoxGotoRelatedItem> {
        if (elements.isEmpty()) return emptySet()
        return elements.mapTo(mutableSetOf()) { ParadoxGotoRelatedItem(it, getGroup()) }.optimized()
    }
}
