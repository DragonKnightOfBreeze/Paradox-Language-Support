package icu.windea.pls.script.codeInsight.parameterInfo

import com.intellij.lang.parameterInfo.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.parameter.*

//com.intellij.codeInsight.hint.api.impls.XmlParameterInfoHandler

/**
 * 在参数上下文引用中显示参数信息。
 */
class ParadoxParameterInfoHandler : ParameterInfoHandler<PsiElement, Collection<ParadoxParameterInfo>> {
    override fun findElementForParameterInfo(context: CreateParameterInfoContext): PsiElement? {
        val element = context.file.findElementAt(context.offset) ?: return null
        val from = ParadoxParameterContextReferenceInfo.From.InContextReference
        val contextReferenceInfo = ParadoxParameterSupport.findContextReferenceInfo(element, from, context.offset) ?: return null
        val targetElement = contextReferenceInfo.element ?: return null
        val parameterInfosMap = mutableMapOf<String, Collection<ParadoxParameterInfo>>()
        ParadoxParameterSupport.processContext(element, contextReferenceInfo) p@{
            val parameters = ParadoxParameterHandler.getParameters(it)
            if(parameters.isEmpty()) return@p true
            parameterInfosMap.putIfAbsent(parameters.keys.toString(), parameters.values)
            true
        }
        if(parameterInfosMap.isEmpty()) return null
        context.itemsToShow = parameterInfosMap.values.toTypedArray()
        return targetElement
    }
    
    override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): PsiElement? {
        val element = context.file.findElementAt(context.offset) ?: return null
        val from = ParadoxParameterContextReferenceInfo.From.InContextReference
        val contextReferenceInfo = ParadoxParameterSupport.findContextReferenceInfo(element, from, context.offset) ?: return null
        val targetElement = contextReferenceInfo.element ?: return null
        val current = context.parameterOwner
        if(current != null && current !== targetElement) return null
        return targetElement
    }
    
    override fun updateUI(parameterInfos: Collection<ParadoxParameterInfo>, context: ParameterInfoUIContext) {
        //PARAM1, PARAM2, ...
        //不高亮特定的参数
        var isFirst = true
        val text = if(parameterInfos.isEmpty()) PlsBundle.message("noParameters") else buildString {
            for(info in parameterInfos) {
                if(isFirst) isFirst = false else append(", ")
                append(info.name)
                if(info.optional) append("?") //optional marker
            }
        }
        val startOffset = 0
        val endOffset = 0
        context.setupUIComponentPresentation(text, startOffset, endOffset, false, false, false, context.defaultParameterColor)
    }
    
    override fun updateParameterInfo(parameterOwner: PsiElement, context: UpdateParameterInfoContext) {
        context.parameterOwner = parameterOwner
    }
    
    override fun showParameterInfo(element: PsiElement, context: CreateParameterInfoContext) {
        context.showHint(element, element.startOffset + 1, this)
    }
}
