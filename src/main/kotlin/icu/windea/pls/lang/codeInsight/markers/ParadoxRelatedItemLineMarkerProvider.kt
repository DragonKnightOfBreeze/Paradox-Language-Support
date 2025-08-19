package icu.windea.pls.lang.codeInsight.markers

import com.intellij.codeInsight.daemon.*
import com.intellij.codeInsight.navigation.*
import com.intellij.navigation.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.util.*
import javax.swing.*

abstract class ParadoxRelatedItemLineMarkerProvider : RelatedItemLineMarkerProvider() {
    companion object {
        private val DEFAULT_PSI_CONVERTOR = NotNullFunction<PsiElement, Collection<PsiElement>> { it.singleton.setOrEmpty() }
    }

    protected fun createNavigationGutterIconBuilder(icon: Icon, gotoRelatedItemProvider: (PsiElement) -> Collection<GotoRelatedItem>): NavigationGutterIconBuilder<PsiElement> {
        return NavigationGutterIconBuilder.create(icon, DEFAULT_PSI_CONVERTOR, gotoRelatedItemProvider)
    }
}
