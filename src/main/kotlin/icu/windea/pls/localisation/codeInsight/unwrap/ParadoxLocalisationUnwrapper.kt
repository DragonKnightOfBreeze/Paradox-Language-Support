package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.AbstractUnwrapper
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import icu.windea.pls.localisation.psi.ParadoxLocalisationPsiService

abstract class ParadoxLocalisationUnwrapper : AbstractUnwrapper<ParadoxLocalisationUnwrapper.Context>("") {
    abstract override fun isApplicableTo(element: PsiElement): Boolean

    override fun getDescription(element: PsiElement): String {
        return super.getDescription(element)
    }

    override fun createContext(): Context {
        return Context()
    }

    class Context : AbstractContext() {
        override fun isWhiteSpace(element: PsiElement?): Boolean {
            return element is PsiWhiteSpace
        }

        fun extract(element: PsiElement, containerElement: PsiElement) {
            val first = ParadoxLocalisationPsiService.findStartElementToExtract(containerElement) ?: return
            val last = ParadoxLocalisationPsiService.findEndElementToExtract(containerElement) ?: return
            var toExtract = first
            if (isEffective) {
                toExtract = addRangeBefore(first, last, element.parent, element)
            }
            var current: PsiElement? = first
            do {
                addElementToExtract(toExtract)
                toExtract = toExtract.nextSibling
                current = current?.nextSibling
            } while (current != null && current.prevSibling !== last)
        }
    }
}
