package icu.windea.pls.lang.injection

import com.intellij.lang.injection.*
import com.intellij.psi.*
import com.intellij.util.InjectionUtils
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

class ParadoxScriptStringExpressionInjector: MultiHostInjector {
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
        
        //inject CWT config context to injection host
        //TODO 1.1.0+
    }
}