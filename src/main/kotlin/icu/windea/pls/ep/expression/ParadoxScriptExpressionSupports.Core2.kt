package icu.windea.pls.ep.expression

import com.intellij.codeInsight.completion.*
import com.intellij.lang.annotation.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.ep.parameter.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.references.*

class ParadoxParameterScriptExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataTypes.Parameter
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptAttributesKeys.ARGUMENT_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        CwtConfigHandler.annotateScriptExpression(element, range, attributesKey, holder)
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        return ParadoxParameterSupport.resolveArgument(element, rangeInElement, config)
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        if(context.keyword.isParameterized()) return //排除可能带参数的情况
        
        val config = context.config ?: return
        //提示参数名（仅限key）
        val contextElement = context.contextElement!!
        val isKey = context.isKey
        if(isKey != true || config !is CwtPropertyConfig) return
        ParadoxParameterHandler.completeArguments(contextElement, context, result)
    }
}

class ParadoxLocalisationParameterScriptExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(config: CwtConfig<*>): Boolean {
        return config.expression?.type == CwtDataTypes.LocalisationParameter
    }
    
    override fun annotate(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = ParadoxScriptAttributesKeys.ARGUMENT_KEY
        val textRange = element.textRange
        val range = rangeInElement?.shiftRight(textRange.startOffset) ?: textRange.unquote(element.text)
        CwtConfigHandler.annotateScriptExpression(element, range, attributesKey, holder)
    }
    
    override fun resolve(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, expression: String, config: CwtConfig<*>, isKey: Boolean?, exact: Boolean): PsiElement? {
        return ParadoxLocalisationParameterSupport.resolveArgument(element, rangeInElement, config)
    }
    
    override fun complete(context: ProcessingContext, result: CompletionResultSet) {
        //NOTE 不兼容本地化参数（CwtDataTypes.LocalisationParameter），因为那个引用实际上也可能对应一个缺失的本地化的名字
    }
}
