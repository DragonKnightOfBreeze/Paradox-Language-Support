package icu.windea.pls.lang.codeInsight.markers

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.openapi.util.NlsContexts.*
import com.intellij.psi.PsiElement

abstract class ParadoxRelatedItemLineMarkerProvider : RelatedItemLineMarkerProvider() {
    abstract fun getGroup(): @Separator String

    protected fun createGotoRelatedItem(elements: Collection<PsiElement>): List<ParadoxGotoRelatedItem> {
        return elements.mapTo(mutableListOf()) { ParadoxGotoRelatedItem(it, getGroup()) }
    }
}
