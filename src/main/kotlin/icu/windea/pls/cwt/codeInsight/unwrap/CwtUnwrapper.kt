package icu.windea.pls.cwt.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.AbstractUnwrapper
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.siblings
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.cwt.psi.CwtValue

abstract class CwtUnwrapper : AbstractUnwrapper<CwtUnwrapper.Context>("") {
    override fun createContext(): Context {
        return Context()
    }

    class Context : AbstractContext() {
        override fun isWhiteSpace(element: PsiElement?): Boolean {
            return element is PsiWhiteSpace
        }

        fun extract(element: PsiElement, containerElement: PsiElement) {
            var first = containerElement.firstChild.siblings(forward = true).find { isElementToExtract(it) }
            val last = containerElement.lastChild.siblings(forward = false).find { isElementToExtract(it) }
            if (first == null || last == null) return
            var toExtract = first
            if (isEffective) {
                toExtract = addRangeBefore(first, last, element.parent, element)
            }

            do {
                if (toExtract != null) {
                    addElementToExtract(toExtract)
                    toExtract = toExtract.nextSibling
                }
                first = first?.nextSibling
            } while (first != null && first.prevSibling !== last)
        }

        private fun isElementToExtract(element: PsiElement): Boolean {
            return element is PsiComment || element is CwtProperty || element is CwtValue
        }
    }
}
