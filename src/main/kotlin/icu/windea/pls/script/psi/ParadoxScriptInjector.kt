package icu.windea.pls.script.psi

import com.intellij.lang.injection.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.script.*

/**
 * 脚本语言的语言注入器，用于提供以下功能：
 * 
 * * 对脚本参数的传入值进行语言注入（注入为脚本片段），以便推断对应的CWT规则上下文，从而提供高级语言功能。
 * * 对脚本参数的默认值进行语言注入（注入为脚本片段），以便推断对应的CWT规则上下文，从而提供高级语言功能。
 */
class ParadoxScriptInjector : MultiHostInjector {
    //see: com.intellij.util.InjectionUtils
    //see: com.intellij.psi.impl.source.tree.injected.InjectedFileViewProvider
    //see: org.intellij.plugins.intelliLang.inject.InjectorUtils
    
    companion object {
        private val toInject = listOf(ParadoxScriptString::class.java)
    }
    
    override fun elementsToInjectIn(): List<Class<out PsiElement>> {
        return toInject
    }
    
    override fun getLanguagesToInject(registrar: MultiHostRegistrar, host: PsiElement) {
        if(host.hasSyntaxError()) return //skip if host has syntax error 
        
        applyInjectionForArgumentValue(registrar, host)
        applyInjectionForParameterDefaultValue(registrar, host)
    }
    
    private fun applyInjectionForArgumentValue(registrar: MultiHostRegistrar, host: PsiElement) {
        if(host !is ParadoxScriptString) return
        if(!getSettings().inference.parameterConfig) return
        val rangesInsideHost = getRangeInsideHostForArgumentValue(host)
        if(rangesInsideHost.isEmpty()) return
        
        rangesInsideHost.forEach { rangeInsideHost ->
            registrar.startInjecting(ParadoxScriptLanguage)
            registrar.addPlace(null, null, host, rangeInsideHost)
            registrar.doneInjecting()
        }
        InjectionUtils.enableInjectLanguageAction(host, false)
        host.containingFile?.let { file -> InjectionUtils.setCollectLineMarkersForInjectedFiles(file, false) }
    }
    
    private fun getRangeInsideHostForArgumentValue(host: ParadoxScriptString): List<TextRange> {
        //这里先向上得到contextReferenceInfo，接着获取传入值对应的textRange，然后选用在host的textRange之内的那些
        //这里可能存在一些性能问题……需要确认
        val from = ParadoxParameterContextReferenceInfo.From.InContextReference
        val contextReferenceInfo = ParadoxParameterSupport.getContextReferenceInfo(host, from = from) ?: return emptyList()
        if(contextReferenceInfo.arguments.isEmpty()) return emptyList()
        val textRange = host.textRange
        return contextReferenceInfo.arguments.mapNotNull { referenceInfo -> 
            referenceInfo.argumentValueRange
                ?.takeIf { it.startOffset >= textRange.startOffset && it.endOffset <= textRange.endOffset }
                ?.shiftLeft(textRange.startOffset)
        }
    }
    
    private fun applyInjectionForParameterDefaultValue(registrar: MultiHostRegistrar, host: PsiElement) {
        if(host !is ParadoxParameter) return
        if(!getSettings().inference.parameterConfig) return
        val rangeInsideHost = getRangeInsideHostForParameterDefaultValue(host)
        if(rangeInsideHost == null) return
        
        registrar.startInjecting(ParadoxScriptLanguage)
        registrar.addPlace(null, null, host, rangeInsideHost)
        registrar.doneInjecting()
        InjectionUtils.enableInjectLanguageAction(host, false)
        host.containingFile?.let { file -> InjectionUtils.setCollectLineMarkersForInjectedFiles(file, false) }
    }
    
    private fun getRangeInsideHostForParameterDefaultValue(host: ParadoxParameter): TextRange? {
        var start = -1
        var end = -1
        host.processChild { e ->
            val elementType = e.elementType
            if(elementType == ParadoxScriptElementTypes.PIPE) {
                start = e.startOffsetInParent + 1
            } else if(elementType == ParadoxScriptElementTypes.PARAMETER_END && start != -1) {
                end = e.startOffsetInParent
            }
            true
        }
        if(start == -1 || end == -1) return null
        return TextRange.create(start, end)
    }
}