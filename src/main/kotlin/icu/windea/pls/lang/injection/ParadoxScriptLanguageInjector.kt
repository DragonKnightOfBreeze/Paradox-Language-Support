package icu.windea.pls.lang.injection

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.util.InjectionUtils
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.model.injection.ParadoxLocalisationTextInjectionInfo
import icu.windea.pls.model.injection.ParadoxParameterValueInjectionInfo
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptString

/**
 * 脚本语言的语言注入器。
 *
 * - 对脚本参数的传入值进行语言注入（注入为脚本片段），以便推断对应的规则上下文，从而提供高级语言功能。
 * - 对脚本参数的默认值进行语言注入（注入为脚本片段），以便推断对应的规则上下文，从而提供高级语言功能。
 * - 如有必要，对用引号括起的字符串进行语言注入（注入为本地化文本），以便识别其中的富文本语法。
 *
 * @see ParadoxScriptInjectionManager
 * @see ParadoxParameterValueInjectionInfo
 * @see ParadoxLocalisationTextInjectionInfo
 */
class ParadoxScriptLanguageInjector : MultiHostInjector {
    // see com.intellij.util.InjectionUtils
    // see com.intellij.psi.impl.source.tree.injected.InjectedFileViewProvider
    // see org.intellij.plugins.intelliLang.inject.InjectorUtils

    private val toInject = listOf(
        ParadoxScriptString::class.java,
        ParadoxParameter::class.java
    )

    override fun elementsToInjectIn() = toInject

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, host: PsiElement) {
        if (host !is PsiLanguageInjectionHost) return
        InjectionUtils.enableInjectLanguageAction(host, false) // disable inject language action

        runCatchingCancelable { doGetLanguageToInject(host, registrar) }
            .onFailure { e -> thisLogger().error(e.message, e) }
    }

    private fun doGetLanguageToInject(host: PsiLanguageInjectionHost, registrar: MultiHostRegistrar) {
        if (host.lastChild is PsiErrorElement) return // skip if host has syntax error

        val parameterValueInjectionInfos = ParadoxScriptInjectionManager.applyParameterValueInjection(host)
        if (parameterValueInjectionInfos.isNotEmpty()) {
            for (injectionInfo in parameterValueInjectionInfos) {
                registrar.startInjecting(ParadoxScriptLanguage)
                registrar.addPlace(null, null, host, injectionInfo.rangeInsideHost)
                registrar.doneInjecting()
            }
            return
        }

        val localisationTextInjectionInfos = ParadoxScriptInjectionManager.applyLocalisationTextInjection(host)
        if (localisationTextInjectionInfos.isNotEmpty()) {
            for (injectionInfo in localisationTextInjectionInfos) {
                registrar.startInjecting(ParadoxLocalisationLanguage)
                registrar.addPlace("${injectionInfo.localisationName}: \"", "\"", host, injectionInfo.rangeInsideHost)
                registrar.doneInjecting()
            }
        }
    }
}
