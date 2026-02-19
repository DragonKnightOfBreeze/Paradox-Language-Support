package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.AbstractUnwrapper
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import icu.windea.pls.core.children
import icu.windea.pls.localisation.psi.ParadoxLocalisationRichText

abstract class ParadoxLocalisationUnwrapper : AbstractUnwrapper<ParadoxLocalisationUnwrapper.Context>("") {
    override fun createContext(): Context {
        return Context()
    }

    class Context : AbstractContext() {
        override fun isWhiteSpace(element: PsiElement?): Boolean {
            return element is PsiWhiteSpace
        }

        fun extract(element: PsiElement, containerElement: PsiElement) {
            val first = containerElement.children(forward = true).find { isElementToExtract(element, it) } ?: return
            val last = containerElement.children(forward = false).find { isElementToExtract(element, it) } ?: return
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

        @Suppress("unused")
        private fun isElementToExtract(element: PsiElement, child: PsiElement): Boolean {
            return child is ParadoxLocalisationRichText
        }
    }
}
