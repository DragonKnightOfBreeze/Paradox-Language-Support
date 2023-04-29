package icu.windea.pls.script.codeInsight.unwrap

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.script.psi.*

object ParadoxScriptUnwrappers {
    class ParadoxScriptScriptedVariableRemover(key: String) : ParadoxScriptRemover(key) {
        override fun getName(e: PsiElement): String {
            return when(e) {
                is ParadoxScriptScriptedVariable -> e.text.removePrefix("@")
                else -> ""
            }
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxScriptScriptedVariable
        }
    }
    
    class ParadoxScriptPropertyRemover(key: String) : ParadoxScriptRemover(key) {
        override fun getName(e: PsiElement): String {
            return when(e) {
                is ParadoxScriptProperty -> e.text.unquote()
                else -> ""
            }
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxScriptProperty
        }
    }
    
    class ParadoxScriptValueRemover(key: String) : ParadoxScriptRemover(key) {
        override fun getName(e: PsiElement): String {
            return when(e) {
                is ParadoxScriptString -> e.text.unquote()
                is ParadoxScriptValue -> e.value
                else -> ""
            }
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxScriptValue && e.isBlockValue()
        }
    }
    
    class ParadoxScriptParameterConditionRemover(key: String) : ParadoxScriptRemover(key) {
        override fun getName(e: PsiElement): String {
            return when(e) {
                is ParadoxScriptParameterCondition -> e.conditionExpression?.let { PlsConstants.parameterConditionFolder(it) } ?: PlsConstants.unresolvedString
                else -> ""
            }
        }
        
        override fun isApplicableTo(e: PsiElement): Boolean {
            return e is ParadoxScriptParameterCondition
        }
    }
}