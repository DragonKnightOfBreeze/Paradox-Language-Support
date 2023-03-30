package icu.windea.pls.cwt.codeInsight.unwrap

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

object CwtUnwrappers {
    class CwtPropertyRemover(key: String) : CwtRemover(key) {
        override fun getName(e: PsiElement): String {
            return if(e is CwtProperty) e.name else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is CwtProperty
        }
    }
    
    class CwtValueRemover(key: String) : CwtRemover(key) {
        override fun getName(e: PsiElement): String {
            return if(e is CwtValue) e.name else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is CwtValue && e.isBlockValue()
        }
    }
}