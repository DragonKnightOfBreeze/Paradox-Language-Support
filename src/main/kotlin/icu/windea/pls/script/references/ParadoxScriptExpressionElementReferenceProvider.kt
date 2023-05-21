package icu.windea.pls.script.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

class ParadoxScriptExpressionElementReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
        //尝试兼容可能包含参数的情况
        //if(text.isParameterized()) return PsiReference.EMPTY_ARRAY
        
        val isKey = element is ParadoxScriptPropertyKey
        
        //尝试解析为复杂枚举值声明
        if(element is ParadoxScriptStringExpressionElement) {
            val complexEnumValueInfo = ParadoxComplexEnumValueHandler.getInfo(element)
            if(complexEnumValueInfo != null) {
                val project = element.project
                val config = complexEnumValueInfo.getConfig(project)
                if(config != null) {
                    val text = element.text
                    val textRange = TextRange.create(0, text.length).unquote(text) //unquoted text
                    val reference = ParadoxComplexEnumValuePsiReference(element, textRange, complexEnumValueInfo, project)
                    return arrayOf(reference)
                }
            }
        }
        
        //尝试基于CWT规则进行解析
        val configs = ParadoxConfigHandler.getConfigs(element, !isKey, isKey)
        val config = configs.firstOrNull()
        if(config != null) {
            if(element !is ParadoxScriptExpressionElement) return PsiReference.EMPTY_ARRAY
            val text = element.text
            val textRange = getTextRange(element, text)
            val reference = ParadoxScriptExpressionPsiReference(element, textRange, config, isKey)
            return collectReferences(reference)
        }
        
        return PsiReference.EMPTY_ARRAY
    }
    
    private fun collectReferences(reference: PsiReference): Array<out PsiReference> {
        if(reference is PsiReferencesAware) {
            val result = SmartList<PsiReference>()
            doCollectReferences(reference, result)
            return result.toTypedArray()
        }
        return arrayOf(reference)
    }
    
    private fun doCollectReferences(sourceReference: PsiReference, result: MutableList<PsiReference>) {
        if(sourceReference is PsiReferencesAware) {
            val references = sourceReference.getReferences()
            if(references.isNotNullOrEmpty()) {
                for(reference in references) {
                    doCollectReferences(reference, result)
                }
                return
            }
        }
        result.add(sourceReference)
    }
    
    private fun getTextRange(element: PsiElement, text: String): TextRange {
        return when {
            element is ParadoxScriptBlock -> TextRange.create(0, 1) //left curly brace
            else -> TextRange.create(0, text.length).unquote(text) //unquoted text
        }
    }
}
