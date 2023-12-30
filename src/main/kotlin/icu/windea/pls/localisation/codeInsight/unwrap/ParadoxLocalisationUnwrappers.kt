package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.psi.*
import icu.windea.pls.localisation.psi.*

object ParadoxLocalisationUnwrappers {
    class ParadoxLocalisationPropertyRemover(key: String) : ParadoxLocalisationUnwraper(key) {
        override fun getName(e: PsiElement): String {
            return if(e is ParadoxLocalisationProperty) e.name else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxLocalisationProperty
        }
        
        override fun doUnwrap(element: PsiElement, context: Context) {
            context.delete(element)
        }
    }
    
    class ParadoxLocalisationIconRemover(key: String) : ParadoxLocalisationUnwraper(key) {
        override fun getName(e: PsiElement): String {
            return if(e is ParadoxLocalisationIcon) e.name.orEmpty() else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxLocalisationIcon
        }
        
        override fun doUnwrap(element: PsiElement, context: Context) {
            context.delete(element)
        }
    }
    
    class ParadoxLocalisationCommandRemover(key: String) : ParadoxLocalisationUnwraper(key) {
        override fun getName(e: PsiElement): String {
            return if(e is ParadoxLocalisationCommand) e.text.removePrefix("[").removeSuffix("]") else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxLocalisationCommand
        }
        
        override fun doUnwrap(element: PsiElement, context: Context) {
            context.delete(element)
        }
    }
    
    class ParadoxLocalisationReferenceRemover(key: String) : ParadoxLocalisationUnwraper(key) {
        override fun getName(e: PsiElement): String {
            return if(e is ParadoxLocalisationPropertyReference) e.name else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxLocalisationPropertyReference
        }
        
        override fun doUnwrap(element: PsiElement, context: Context) {
            context.delete(element)
        }
    }
    
    class ParadoxLocalisationColorfulTextRemover(key: String) : ParadoxLocalisationUnwraper(key) {
        override fun getName(e: PsiElement): String {
            return if(e is ParadoxLocalisationColorfulText) e.name.orEmpty() else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxLocalisationColorfulText
        }
        
        override fun doUnwrap(element: PsiElement, context: Context) {
            context.delete(element)
        }
    }
    
    class ParadoxLocalisationColorfulTextUnwrapper(key: String) : ParadoxLocalisationUnwraper(key) {
        override fun getName(e: PsiElement): String {
            return if(e is ParadoxLocalisationColorfulText) e.name.orEmpty() else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxLocalisationColorfulText
        }
        
        override fun doUnwrap(element: PsiElement, context: Context) {
            context.extract(element, element)
            context.delete(element)
        }
    }
}