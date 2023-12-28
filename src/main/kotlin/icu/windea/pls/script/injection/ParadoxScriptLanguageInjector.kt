package icu.windea.pls.script.injection

import com.intellij.lang.injection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.model.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

/**
 * 脚本语言的语言注入器，用于提供以下功能：
 *
 * * 对脚本参数的传入值进行语言注入（注入为脚本片段），以便推断对应的CWT规则上下文，从而提供高级语言功能。
 * * 对脚本参数的默认值进行语言注入（注入为脚本片段），以便推断对应的CWT规则上下文，从而提供高级语言功能。
 * 
 * @see icu.windea.pls.lang.config.CwtParameterValueConfigContextProvider
 */
class ParadoxScriptLanguageInjector : MultiHostInjector {
    //see: com.intellij.util.InjectionUtils
    //see: com.intellij.psi.impl.source.tree.injected.InjectedFileViewProvider
    //see: org.intellij.plugins.intelliLang.inject.InjectorUtils
    
    private val toInject = listOf(
        ParadoxScriptStringExpressionElement::class.java,
        ParadoxParameter::class.java
    )
    
    override fun elementsToInjectIn(): List<Class<out PsiElement>> {
        return toInject
    }
    
    override fun getLanguagesToInject(registrar: MultiHostRegistrar, host: PsiElement) {
        if(host !is PsiLanguageInjectionHost) return
        InjectionUtils.enableInjectLanguageAction(host, false) //disable inject language action
        
        if(host.hasSyntaxError()) return //skip if host has syntax error 
        
        val allInjectionInfos = mutableListOf<ParameterValueInjectionInfo>()
        
        ProgressManager.checkCanceled()
        applyInjectionForArgumentValue(host, allInjectionInfos)
        ProgressManager.checkCanceled()
        applyInjectionForParameterDefaultValue(host, allInjectionInfos)
        
        host.putUserData(Keys.parameterValueInjectionInfos, allInjectionInfos.orNull())
        if(allInjectionInfos.isEmpty()) return
        allInjectionInfos.forEach { injectionInfo ->
            registrar.startInjecting(ParadoxScriptLanguage)
            registrar.addPlace(null, null, host, injectionInfo.rangeInsideHost)
            registrar.doneInjecting()
        }
    }
    
    private fun applyInjectionForArgumentValue(host: PsiElement, allInjectionInfos: MutableList<ParameterValueInjectionInfo>) {
        if(host !is ParadoxScriptString) return
        val injectionInfos = getInjectionInfosForArgumentValue(host)
        if(injectionInfos.isEmpty()) return
        allInjectionInfos.addAll(injectionInfos)
    }
    
    private fun getInjectionInfosForArgumentValue(host: ParadoxScriptString): List<ParameterValueInjectionInfo> {
        //这里先向上得到contextReferenceInfo，接着获取传入值对应的textRange，然后选用在host的textRange之内的那些
        val from = ParadoxParameterContextReferenceInfo.From.InContextReference
        val contextReferenceInfo = ParadoxParameterSupport.getContextReferenceInfo(host, from = from) ?: return emptyList()
        if(contextReferenceInfo.arguments.isEmpty()) return emptyList()
        val hostRange = host.textRange
        return contextReferenceInfo.arguments.mapNotNull t1@{ referenceInfo ->
            //这里需要特殊处理传入参数值被双引号括起的情况
            val rawRangeInsideHost = referenceInfo.argumentValueRange
                ?.takeIf { it.startOffset >= hostRange.startOffset && it.endOffset <= hostRange.endOffset }
                ?.shiftLeft(hostRange.startOffset)
                ?: return@t1 null
            val rangeInsideHost = rawRangeInsideHost.unquote(rawRangeInsideHost.substring(host.text))
            //这里要求参数值两边都有双引号
            val parameterValueQuoted = rawRangeInsideHost.startOffset != rangeInsideHost.startOffset && rawRangeInsideHost.endOffset != rangeInsideHost.endOffset
            ParameterValueInjectionInfo(rangeInsideHost, parameterValueQuoted) p@{
                val argumentNameElement = referenceInfo.argumentNameElement ?: return@p null
                val argumentNameElementRange = argumentNameElement.textRange
                val argumentNameRange = referenceInfo.argumentNameRange
                    .takeIf { it.startOffset >= argumentNameElementRange.startOffset && it.endOffset <= argumentNameElementRange.endOffset }
                    ?.shiftLeft(argumentNameElementRange.startOffset)
                    ?: return@p null
                argumentNameElement.references.firstNotNullOfOrNull t2@{ reference ->
                    if(reference.rangeInElement != argumentNameRange) return@t2 null
                    reference.resolve()?.castOrNull<ParadoxParameterElement>()
                }
            }
        }
    }
    
    private fun applyInjectionForParameterDefaultValue(host: PsiElement, allInjectionInfos: MutableList<ParameterValueInjectionInfo>) {
        if(host !is ParadoxParameter) return
        val injectionInfo = getInjectionInfoForParameterDefaultValue(host)
        if(injectionInfo == null) return
        allInjectionInfos.add(injectionInfo)
    }
    
    private fun getInjectionInfoForParameterDefaultValue(host: ParadoxParameter): ParameterValueInjectionInfo? {
        val parameterName = host.name
        if(parameterName.isNullOrEmpty()) return null //skip if host is invalid
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
        val rangeInsideHost = TextRange.create(start, end)
        val parameterValueQuoted = false
        return ParameterValueInjectionInfo(rangeInsideHost, parameterValueQuoted) {
            ParadoxParameterSupport.resolveParameter(host)
        }
    }
    
    object Keys {
        val parameterValueInjectionInfos = createKey<List<ParameterValueInjectionInfo>>("paradox.script.injection.parameterValueInjectionInfo")
    }
}
