package icu.windea.pls.script.codeInsight.parameterInfo

import com.intellij.lang.parameterInfo.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.parameter.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.*

//com.intellij.codeInsight.hint.api.impls.XmlParameterInfoHandler

/**
 * 在参数上下文引用中显示参数信息。
 */
class ParadoxParameterInfoHandler : ParameterInfoHandler<PsiElement, ParadoxParameterContextInfo> {
    override fun findElementForParameterInfo(context: CreateParameterInfoContext): PsiElement? {
        val element = context.file.findElementAt(context.offset) ?: return null
        val from = ParadoxParameterContextReferenceInfo.From.InContextReference
        val contextReferenceInfo = ParadoxParameterSupport.getContextReferenceInfo(element, from, context.offset) ?: return null
        val targetElement = contextReferenceInfo.element ?: return null
        val parameterContextInfoMap = mutableMapOf<String, ParadoxParameterContextInfo>()
        ParadoxParameterSupport.processContext(element, contextReferenceInfo, true) p@{
            ProgressManager.checkCanceled()
            val parameterContextInfo = ParadoxParameterSupport.getContextInfo(it) ?: return@p true
            if (parameterContextInfo.parameters.isEmpty()) return@p true
            parameterContextInfoMap.putIfAbsent(parameterContextInfo.parameters.keys.toString(), parameterContextInfo)
            true
        }
        if (parameterContextInfoMap.isEmpty()) return null
        context.itemsToShow = parameterContextInfoMap.values.toTypedArray()
        return targetElement
    }

    override fun findElementForUpdatingParameterInfo(context: UpdateParameterInfoContext): PsiElement? {
        val element = context.file.findElementAt(context.offset) ?: return null
        val from = ParadoxParameterContextReferenceInfo.From.InContextReference
        val contextReferenceInfo = ParadoxParameterSupport.getContextReferenceInfo(element, from, context.offset) ?: return null
        val targetElement = contextReferenceInfo.element ?: return null
        val current = context.parameterOwner
        if (current != null && current !== targetElement) return null
        return targetElement
    }

    override fun updateUI(parameterContextInfo: ParadoxParameterContextInfo, context: ParameterInfoUIContext) {
        //PARAM1, PARAM2, ...
        //不高亮特定的参数
        val text = when {
            parameterContextInfo.parameters.isEmpty() -> PlsBundle.message("noParameters")
            else -> {
                buildString {
                    var isFirst = true
                    parameterContextInfo.parameters.forEach { (parameterName, elements) ->
                        if (isFirst) isFirst = false else append(", ")
                        append(parameterName)
                        if (ParadoxParameterManager.isOptional(parameterContextInfo, parameterName)) append("?") //optional marker
                        //加上推断得到的类型信息
                        val parameterElement = elements.firstOrNull()?.parameterElement
                        if (parameterElement != null) {
                            val inferredType = ParadoxParameterManager.getInferredType(parameterElement)
                            if (inferredType != null) {
                                append(": ").append(inferredType.escapeXml())
                            }
                        }
                    }
                }
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
