package icu.windea.pls.localisation.references

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.references.*

class ParadoxLocalisationExpressionReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
        ProgressManager.checkCanceled()
        
        if(element !is ParadoxLocalisationExpressionElement) return PsiReference.EMPTY_ARRAY
        
        //尝试兼容可能包含参数的情况
        //if(text.isParameterized()) return PsiReference.EMPTY_ARRAY
        
        ////尝试基于CWT规则进行解析
        //run {
        //    val isKey = element is ParadoxScriptPropertyKey
        //    val configs = ParadoxExpressionHandler.getConfigs(element, orDefault = isKey)
        //    val config = configs.firstOrNull() ?: return@run
        //    val textRange = ParadoxExpressionHandler.getExpressionTextRange(element) //unquoted text
        //    val reference = ParadoxScriptExpressionPsiReference(element, textRange, config, isKey)
        //    return reference.collectReferences()
        //}
        
        return PsiReference.EMPTY_ARRAY
    }
}
