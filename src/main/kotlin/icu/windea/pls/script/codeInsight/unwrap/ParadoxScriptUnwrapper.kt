package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.script.psi.*

abstract class ParadoxScriptUnwrapper : AbstractUnwrapper<ParadoxScriptUnwrapper.Context>("") {
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
            return element is PsiComment || element is ParadoxScriptProperty || element is ParadoxScriptValue
                || element is ParadoxScriptScriptedVariable
                || element is ParadoxScriptParameterCondition
                || element is ParadoxScriptInlineParameterCondition
        }
    }
}
