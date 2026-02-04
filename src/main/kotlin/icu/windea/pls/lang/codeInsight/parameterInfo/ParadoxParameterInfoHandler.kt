package icu.windea.pls.lang.codeInsight.parameterInfo

import com.intellij.lang.parameterInfo.CreateParameterInfoContext
import com.intellij.lang.parameterInfo.ParameterInfoHandler
import com.intellij.lang.parameterInfo.ParameterInfoUIContext
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.startOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.util.OnceMarker
import icu.windea.pls.lang.resolve.ParadoxParameterService
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.model.ParadoxParameterContextInfo
import icu.windea.pls.model.ParadoxParameterContextReferenceInfo

/**
 * 在参数上下文引用中显示参数信息。
 */
class ParadoxParameterInfoHandler : ParameterInfoHandler<PsiElement, ParadoxParameterContextInfo> {
    override fun findElementForParameterInfo(context: CreateParameterInfoContext): PsiElement? {
        val element = context.file.findElementAt(context.offset) ?: return null
        val from = ParadoxParameterContextReferenceInfo.From.InContextReference
        val contextReferenceInfo = ParadoxParameterService.getContextReferenceInfo(element, from, context.offset) ?: return null
        val targetElement = contextReferenceInfo.element ?: return null
        val parameterContextInfoMap = mutableMapOf<String, ParadoxParameterContextInfo>()
        ParadoxParameterService.processContextReference(element, contextReferenceInfo, true) p@{
            ProgressManager.checkCanceled()
            val parameterContextInfo = ParadoxParameterService.getContextInfo(it) ?: return@p true
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
        val contextReferenceInfo = ParadoxParameterService.getContextReferenceInfo(element, from, context.offset) ?: return null
        val targetElement = contextReferenceInfo.element ?: return null
        val current = context.parameterOwner
        if (current != null && current !== targetElement) return null
        return targetElement
    }

    override fun updateUI(parameterContextInfo: ParadoxParameterContextInfo, context: ParameterInfoUIContext) {
        // PARAM1, PARAM2, ...
        // 不高亮特定的参数
        val text = when {
            parameterContextInfo.parameters.isEmpty() -> PlsBundle.message("noParameters")
            else -> buildString {
                val m = OnceMarker()
                parameterContextInfo.parameters.forEach { (parameterName, elements) ->
                    if (m.mark()) append(", ")
                    append(parameterName)
                    val optional = ParadoxParameterManager.isOptional(parameterContextInfo, parameterName)
                    if (optional) append("?") // optional marker
                    // 加上推断得到的类型信息
                    val parameterElement = elements.firstOrNull()?.parameterElement
                    val inferredType = parameterElement?.let { ParadoxParameterManager.getInferredType(it) }
                    if (inferredType.isNotNullOrEmpty()) append(": ").append(inferredType)
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
