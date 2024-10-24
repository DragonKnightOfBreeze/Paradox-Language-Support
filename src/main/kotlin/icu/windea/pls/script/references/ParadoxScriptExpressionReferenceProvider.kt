package icu.windea.pls.script.references

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

class ParadoxScriptExpressionReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
        ProgressManager.checkCanceled()

        if (element !is ParadoxScriptExpressionElement || !element.isExpression()) return PsiReference.EMPTY_ARRAY

        //尝试兼容可能包含参数的情况
        //if(element.text.isParameterized()) return PsiReference.EMPTY_ARRAY

        //尝试解析为复杂枚举值声明
        run {
            if (element !is ParadoxScriptStringExpressionElement) return@run
            val complexEnumValueInfo = ParadoxComplexEnumValueManager.getInfo(element) ?: return@run
            val textRange = ParadoxExpressionManager.getExpressionTextRange(element) //unquoted text
            val reference = ParadoxComplexEnumValuePsiReference(element, textRange, complexEnumValueInfo, element.project)
            return arrayOf(reference)
        }

        //尝试基于CWT规则进行解析
        run {
            val isKey = element is ParadoxScriptPropertyKey
            val configs = ParadoxExpressionManager.getConfigs(element, orDefault = isKey)
            val config = configs.firstOrNull() ?: return@run
            val textRange = ParadoxExpressionManager.getExpressionTextRange(element) //unquoted text
            val reference = ParadoxScriptExpressionPsiReference(element, textRange, config, isKey)
            return reference.collectReferences()
        }

        return PsiReference.EMPTY_ARRAY
    }
}
