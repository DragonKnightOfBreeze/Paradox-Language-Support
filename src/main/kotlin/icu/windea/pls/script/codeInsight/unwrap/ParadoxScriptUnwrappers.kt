package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

object ParadoxScriptUnwrappers {
    class ParadoxScriptScriptedVariableRemover(key: String) : ParadoxScriptUnwrapper(key) {
        override fun getName(e: PsiElement): String {
            return if(e is ParadoxScriptScriptedVariable) e.name.orUnresolved() else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxScriptScriptedVariable
        }
        
        override fun doUnwrap(element: PsiElement, context: Context) {
            context.delete(element)
        }
    }
    
    class ParadoxScriptPropertyRemover(key: String) : ParadoxScriptUnwrapper(key) {
        override fun getName(e: PsiElement): String {
            return if(e is ParadoxScriptProperty) e.name else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxScriptProperty
        }
        
        override fun doUnwrap(element: PsiElement, context: Context) {
            context.delete(element)
        }
    }
    
    class ParadoxScriptValueRemover(key: String) : ParadoxScriptUnwrapper(key) {
        override fun getName(e: PsiElement): String {
            return if(e is ParadoxScriptValue) e.name else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return (e is ParadoxScriptValue && e !is ParadoxScriptBlock) && e.isBlockValue()
        }
        
        override fun doUnwrap(element: PsiElement, context: Context) {
            context.delete(element)
        }
    }
    
    class ParadoxScriptBlockRemover(key: String) : ParadoxScriptUnwrapper(key) {
        override fun getName(e: PsiElement): String {
            return ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxScriptBlock && e.isBlockValue()
        }
        
        override fun doUnwrap(element: PsiElement, context: Context) {
            context.delete(element)
        }
    }
    
    class ParadoxScriptParameterConditionRemover(key: String) : ParadoxScriptUnwrapper(key) {
        override fun getName(e: PsiElement): String {
            return if(e is ParadoxScriptParameterCondition) e.presentationText.orUnresolved() else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxScriptParameterCondition
        }
        
        override fun doUnwrap(element: PsiElement, context: Context) {
            context.delete(element)
        }
    }
    
    class ParadoxScriptInlineParameterConditionRemover(key: String) : ParadoxScriptUnwrapper(key) {
        override fun getName(e: PsiElement): String {
            return if(e is ParadoxScriptInlineParameterCondition) e.presentationText.orUnresolved() else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxScriptInlineParameterCondition
        }
        
        override fun doUnwrap(element: PsiElement, context: Context) {
            context.delete(element)
        }
    }
    
    class ParadoxScriptPropertyUnwrapper(key: String) : ParadoxScriptUnwrapper(key) {
        override fun getName(e: PsiElement): String {
            return if(e is ParadoxScriptProperty) e.name else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxScriptProperty && e.propertyValue is ParadoxScriptBlock
        }
        
        override fun doUnwrap(element: PsiElement, context: Context) {
            if(element !is ParadoxScriptProperty) return
            val block = element.propertyValue
            if(block !is ParadoxScriptBlock) return
            context.extract(element, block)
            context.delete(element)
        }
    }
    
    class ParadoxScriptBlockUnwrapper(key: String) : ParadoxScriptUnwrapper(key) {
        override fun getName(e: PsiElement): String {
            return ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxScriptBlock && e.isBlockValue()
        }
        
        override fun doUnwrap(element: PsiElement, context: Context) {
            if(element !is ParadoxScriptBlock) return
            context.extract(element, element)
            context.delete(element)
        }
    }
    
    class ParadoxScriptParameterConditionUnwrapper(key: String) : ParadoxScriptUnwrapper(key) {
        override fun getName(e: PsiElement): String {
            return if(e is ParadoxScriptParameterCondition) e.presentationText.orUnresolved() else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxScriptParameterCondition
        }
        
        override fun doUnwrap(element: PsiElement, context: Context) {
            context.extract(element, element)
            context.delete(element)
        }
    }
    
    class ParadoxScriptInlineParameterConditionUnwrapper(key: String) : ParadoxScriptUnwrapper(key) {
        override fun getName(e: PsiElement): String {
            return if(e is ParadoxScriptInlineParameterCondition) e.presentationText.orUnresolved() else ""
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxScriptInlineParameterCondition
        }
        
        override fun doUnwrap(element: PsiElement, context: Context) {
            context.extract(element, element)
            context.delete(element)
        }
    }
}