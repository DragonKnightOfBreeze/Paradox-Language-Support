package icu.windea.pls.lang.codeInsight.markers

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.MergeableLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.navigation.GotoRelatedItem
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import javax.swing.Icon

class ParadoxLineMarkerInfo<T : PsiElement>(
    element: T,
    range: TextRange,
    icon: Icon?,
    tooltipProvider: () -> String,
    navHandler: GutterIconNavigationHandler<T>,
    alignment: GutterIconRenderer.Alignment,
    targets: () -> Collection<GotoRelatedItem>,
    accessibleNameProvider: () -> String,
) : RelatedItemLineMarkerInfo<T>(element, range, icon, { _: PsiElement -> tooltipProvider() }, null, navHandler, alignment, targets, accessibleNameProvider) {
    override fun canMergeWith(info: MergeableLineMarkerInfo<*>): Boolean {
        return info is ParadoxLineMarkerInfo && info.icon == icon
    }
}
