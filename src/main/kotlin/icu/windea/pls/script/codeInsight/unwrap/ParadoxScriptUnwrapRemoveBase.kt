package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.*
import com.intellij.psi.*
import icu.windea.pls.*

abstract class ParadoxScriptUnwrapRemoveBase (
    private val key: String
): AbstractUnwrapper<ParadoxScriptUnwrapRemoveBase.Context>("") {
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
    }
}
