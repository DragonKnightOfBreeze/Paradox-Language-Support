package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.AbstractUnwrapper
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.elementType
import icu.windea.pls.core.children
import icu.windea.pls.script.psi.ParadoxScriptElementTypes
import icu.windea.pls.script.psi.ParadoxScriptInlineConditionalBlock
import icu.windea.pls.script.psi.ParadoxScriptNormalConditionalBlock
import icu.windea.pls.script.psi.ParadoxScriptNormalParameter
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptTokenSets
import icu.windea.pls.script.psi.ParadoxScriptTokenType
import icu.windea.pls.script.psi.ParadoxScriptValue

abstract class ParadoxScriptUnwrapper : AbstractUnwrapper<ParadoxScriptUnwrapper.Context>("") {
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
            if (element is ParadoxScriptInlineConditionalBlock) {
                return true
            }
            return child is PsiComment
                || child is ParadoxScriptProperty
                || child is ParadoxScriptValue
                || child is ParadoxScriptScriptedVariable
                || child is ParadoxScriptNormalConditionalBlock
        }
    }
}
