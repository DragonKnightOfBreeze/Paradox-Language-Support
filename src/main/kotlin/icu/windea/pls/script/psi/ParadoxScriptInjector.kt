package icu.windea.pls.script.psi

import com.intellij.lang.injection.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.lang.parameter.*
import icu.windea.pls.script.*

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
        applyInjectionForParameterValue(registrar, host)
    }
    
    private fun applyInjectionForParameterValue(registrar: MultiHostRegistrar, injectionHost: PsiElement) {
        //TODO 1.1.3
        if(injectionHost !is ParadoxScriptString) return
        val argumentNameElement = injectionHost.propertyKey ?: return
        val argumentNameConfig = ParadoxConfigHandler.getConfigs(argumentNameElement).firstOrNull() ?: return
        if(argumentNameConfig.expression.type != CwtDataType.Parameter) return
        val parameterElement = ParadoxParameterSupport.resolveArgument(argumentNameElement, null, argumentNameConfig) ?: return
        
        //unsupported -> don't apply
        val inferredContextConfigs = ParadoxParameterHandler.getInferredContextConfigs(parameterElement)
        if(inferredContextConfigs.singleOrNull() == CwtValueConfig.EmptyConfig) return
        
        registrar.startInjecting(ParadoxScriptLanguage)
        val text = injectionHost.text
        registrar.addPlace(null, null, injectionHost, TextRange.create(0, text.length).unquote(text))
        
        //disable inject language action
        InjectionUtils.enableInjectLanguageAction(injectionHost, false)
        //disable injection background highlight (by implementing InjectionBackgroundSuppressor)
        //inject config context to provide advanced language features
        //see: icu.windea.pls.script.psi.ParadoxScriptParserDefinition.createFile
        
        registrar.doneInjecting()
    }
}