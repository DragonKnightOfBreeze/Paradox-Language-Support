package icu.windea.pls.lang.injection

import com.intellij.lang.injection.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

/**
 * 用于在某些特定场合下注入脚本片段。
 *
 * 后续可以提供CWT规则上下文，以便为注入的脚本片段提供高级语言功能。
 */
class ParadoxScriptSnippetInjector : MultiHostInjector {
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
        if(host !is ParadoxScriptString) return
        val configs = ParadoxConfigResolver.getConfigs(host)
        val config = configs.firstOrNull() ?: return
        when {
            config.expression.type == CwtDataType.ParameterValue -> {
                applyInjectionForParameterValue(registrar, host)
            }
        }
    }
    
    private fun applyInjectionForParameterValue(registrar: MultiHostRegistrar, host: ParadoxScriptString) {
        val text = host.text
        if(!text.let { it.isLeftQuoted() && it.isRightQuoted() }) return
        
        registrar.startInjecting(ParadoxScriptLanguage)
        registrar.addPlace(null, null, host, TextRange.create(0, text.length).unquote(text))
        
        //disable inject language action
        InjectionUtils.enableInjectLanguageAction(host, false)
        //disable injection background highlight (by implementing InjectionBackgroundSuppressor) - do not apply so far
        //inject config context to provide advanced language features
        //see: icu.windea.pls.script.psi.ParadoxScriptParserDefinition.createFile
        
        registrar.doneInjecting()
    }
}