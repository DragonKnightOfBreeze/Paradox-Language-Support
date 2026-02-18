package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.AbstractUnwrapper
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.elementType
import icu.windea.pls.core.children
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptInlineParameterCondition
import icu.windea.pls.script.psi.ParadoxScriptParameter
import icu.windea.pls.script.psi.ParadoxScriptParameterCondition
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptValue

abstract class ParadoxScriptUnwrapper : AbstractUnwrapper<ParadoxScriptUnwrapper.Context>("") {
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

        private fun isElementToExtract(element: PsiElement, child: PsiElement): Boolean {
            if (element is ParadoxScriptInlineParameterCondition) {
                return child.elementType == ParadoxScriptElementTypes.ARGUMENT_TOKEN
                    || child is ParadoxScriptParameter
                    || child is ParadoxScriptInlineParameterCondition
            }
            return child is PsiComment
                || child is ParadoxScriptProperty
                || child is ParadoxScriptValue
                || child is ParadoxScriptScriptedVariable
                || child is ParadoxScriptParameterCondition
        }
    }
}
