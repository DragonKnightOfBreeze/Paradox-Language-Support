package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

abstract class ParadoxLocalisationUnwraper(
    private val key: String
): AbstractUnwrapper<ParadoxLocalisationUnwraper.Context>("") {
    abstract fun getName(e: PsiElement): String
    
    override fun getDescription(e: PsiElement): String {
        return PlsBundle.message(key ,getName(e))
    }
    
    override fun createContext(): Context {
        return Context()
    }
    
    class Context: AbstractContext() {
        override fun isWhiteSpace(element: PsiElement?): Boolean {
            return element is PsiWhiteSpace
        }
        
        fun extract(element: PsiElement, containerElement: PsiElement) {
            var first = containerElement.firstChild.siblings(forward = true).find { isElementToExtract(it) }
            val last = containerElement.lastChild.siblings(forward = false).find { isElementToExtract(it) }
            if(first == null || last == null) return
            var toExtract = first
            if(isEffective) {
                toExtract = addRangeBefore(first, last, element.parent, element)
            }
            
            do {
                if(toExtract != null) {
                    addElementToExtract(toExtract)
                    toExtract = toExtract.nextSibling
                }
                first = first?.nextSibling
            } while(first != null && first.prevSibling !== last)
        }
        
        private fun isElementToExtract(it: PsiElement): Boolean {
            return it is ParadoxLocalisationRichText
        }
    }
}