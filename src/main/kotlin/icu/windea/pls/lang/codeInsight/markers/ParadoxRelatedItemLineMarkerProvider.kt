package icu.windea.pls.lang.codeInsight.markers

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.navigation.GotoRelatedItem
import com.intellij.psi.PsiElement
import com.intellij.util.NotNullFunction
import icu.windea.pls.core.util.setOrEmpty
import icu.windea.pls.core.util.singleton
import javax.swing.Icon

abstract class ParadoxRelatedItemLineMarkerProvider : RelatedItemLineMarkerProvider() {
    companion object {
        private val DEFAULT_PSI_CONVERTOR = NotNullFunction<PsiElement, Collection<PsiElement>> { it.singleton.setOrEmpty() }
    }

    protected fun createNavigationGutterIconBuilder(icon: Icon, gotoRelatedItemProvider: (PsiElement) -> Collection<GotoRelatedItem>): NavigationGutterIconBuilder<PsiElement> {
        return NavigationGutterIconBuilder.create(icon, DEFAULT_PSI_CONVERTOR, gotoRelatedItemProvider)
    }
}
