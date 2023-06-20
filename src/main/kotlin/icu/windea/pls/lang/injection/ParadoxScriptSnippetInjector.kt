package icu.windea.pls.lang.injection

import com.intellij.lang.injection.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

//com.intellij.util.InjectionUtils
//com.intellij.psi.impl.source.tree.injected.InjectedFileViewProvider
//org.intellij.plugins.intelliLang.inject.InjectorUtils

/**
 * 用于在某些特定场合下注入脚本片段。
 * 
 * 后续可以提供CWT规则上下文，以便为注入的脚本片段提供高级语言功能。
 */
class ParadoxScriptSnippetInjector: MultiHostInjector {
     companion object {
         private val toInject = listOf(ParadoxScriptStringExpressionElement::class.java)
     }
    
    override fun elementsToInjectIn(): List<Class<out PsiElement>> {
        return toInject
    }
     
     override fun getLanguagesToInject(registrar: MultiHostRegistrar, host: PsiElement) {
         if(host !is ParadoxScriptStringExpressionElement) return
         val configs = ParadoxConfigResolver.getConfigs(host)
         val config = configs.firstOrNull() ?: return
         when {
             config.expression.type == CwtDataType.ParameterValue -> {
                 //为脚本值对应的表达式提供语言注入（注入脚本语言，提供CWT规则上下文）
                 registrar.startInjecting(ParadoxScriptLanguage)
                 applyInjectionForParameterValue(registrar, host)
                 registrar.doneInjecting()
             }
         }
     }
    
    private fun applyInjectionForParameterValue(registrar: MultiHostRegistrar, host: ParadoxScriptStringExpressionElement) {
        registrar.addPlace(null, null, host, host.textRangeInParent.unquote(host.text))
        //disable inject language action
        InjectionUtils.enableInjectLanguageAction(host, false)
        //disable injection background highlight (by implementing InjectionBackgroundSuppressor)
    }
}