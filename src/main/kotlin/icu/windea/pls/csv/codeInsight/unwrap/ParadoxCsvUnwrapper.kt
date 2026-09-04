package icu.windea.pls.csv.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.AbstractUnwrapper
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace

abstract class ParadoxCsvUnwrapper : AbstractUnwrapper<ParadoxCsvUnwrapper.Context>("") {
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
    }
}
