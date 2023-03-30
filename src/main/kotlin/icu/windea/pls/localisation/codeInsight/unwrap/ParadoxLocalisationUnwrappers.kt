package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.psi.*
import icu.windea.pls.localisation.psi.*

object ParadoxLocalisationUnwrappers {
    class ParadoxLocalisationPropertyRemover(key: String) : ParadoxLocalisationRemover(key) {
        override fun getName(e: PsiElement): String {
            return if(e is ParadoxLocalisationProperty) e.name else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxLocalisationProperty
        }
    }
    
    class ParadoxLocalisationIconRemover(key: String) : ParadoxLocalisationRemover(key) {
        override fun getName(e: PsiElement): String {
            return if(e is ParadoxLocalisationIcon) e.name.orEmpty() else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxLocalisationIcon
        }
    }
    
    class ParadoxLocalisationCommandRemover(key: String) : ParadoxLocalisationRemover(key) {
        override fun getName(e: PsiElement): String {
            return if(e is ParadoxLocalisationCommand) e.text.removePrefix("[").removeSuffix("]") else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxLocalisationCommand
        }
    }
    
    class ParadoxLocalisationReferenceRemover(key: String) : ParadoxLocalisationRemover(key) {
        override fun getName(e: PsiElement): String {
            return if(e is ParadoxLocalisationPropertyReference) e.name else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxLocalisationPropertyReference
        }
    }
    
    class ParadoxLocalisationColorfulTextRemover(key: String) : ParadoxLocalisationRemover(key) {
        override fun getName(e: PsiElement): String {
            return if(e is ParadoxLocalisationColorfulText) e.name.orEmpty() else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxLocalisationColorfulText
        }
    }
    
    class ParadoxLocalisationColorfulTextUnwrapper(key: String) : ParadoxLocalisationUnwrapRemoveBase(key) {
        override fun getName(e: PsiElement): String {
            return if(e is ParadoxLocalisationColorfulText) e.name.orEmpty() else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxLocalisationColorfulText
        }
        
        override fun doUnwrap(element: PsiElement, context: Context) {
            if(element is ParadoxLocalisationColorfulText) {
                context.extract(element)
                context.delete(element)
            }
        }
    }
}