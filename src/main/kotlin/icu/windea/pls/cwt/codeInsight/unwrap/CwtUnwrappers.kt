package icu.windea.pls.cwt.codeInsight.unwrap

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

object CwtUnwrappers {
    class CwtPropertyRemover(key: String) : CwtUnwrapper(key) {
        override fun getName(e: PsiElement): String {
            return if(e is CwtProperty) e.name else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is CwtProperty
        }
        
        override fun doUnwrap(element: PsiElement, context: Context) {
            context.delete(element)
        }
    }
    
    class CwtValueRemover(key: String) : CwtUnwrapper(key) {
        override fun getName(e: PsiElement): String {
            return if(e is CwtValue) e.name else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return (e is CwtValue && e !is CwtBlock) && e.isBlockValue()
        }
        
        override fun doUnwrap(element: PsiElement, context: Context) {
            context.delete(element)
        }
    }
    
    class CwtBlockRemover(key: String) : CwtUnwrapper(key) {
        override fun getName(e: PsiElement): String {
            return ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is CwtBlock && e.isBlockValue()
        }
        
        override fun doUnwrap(element: PsiElement, context: Context) {
            context.delete(element)
        }
    }
    
    class CwtPropertyUnwrapper(key: String): CwtUnwrapper(key) {
        override fun getName(e: PsiElement): String {
            return if(e is CwtProperty) e.name else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is CwtProperty && e.propertyValue is CwtBlock
        }
        
        override fun doUnwrap(element: PsiElement, context: Context) {
            if(element !is CwtProperty) return
            val block = element.propertyValue
            if(block !is CwtBlock) return
            context.extract(element, block)
            context.delete(element)
        }
    }
    
    class CwtBlockUnwrapper(key: String): CwtUnwrapper(key) {
        override fun getName(e: PsiElement): String {
            return ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is CwtBlock && e.isBlockValue()
        }
        
        override fun doUnwrap(element: PsiElement, context: Context) {
            if(element !is CwtBlock) return
            context.extract(element, element)
            context.delete(element)
        }
    }
}