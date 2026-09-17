package icu.windea.pls.lang.injection

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiErrorElement
import com.intellij.util.InjectionUtils
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.lang.psi.ParadoxLanguageInjectionHost
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptNormalParameterArgument
import icu.windea.pls.script.psi.ParadoxScriptString

/**
 * 适用于脚本语言的语言注入器。
 *
 * 对于脚本文件：
 * - 对参数的传入值进行语言注入（注入为脚本片段），以便推断对应的规则上下文，从而提供高级语言功能。
 * - 对参数的默认值进行语言注入（注入为脚本片段），以便推断对应的规则上下文，从而提供高级语言功能。
 * - 如有必要，对用引号括起的字符串进行语言注入（注入为本地化文本），以便识别其中的富文本语法。
 *
 * @see ParadoxLanguageInjectionService
 * @see ParadoxLanguageInjectionManager
 */
@Optimized
class ParadoxLanguageInjector : MultiHostInjector {
    // see: com.intellij.util.InjectionUtils
    // see: com.intellij.psi.impl.source.tree.injected.InjectedFileViewProvider
    // see: org.intellij.plugins.intelliLang.inject.InjectorUtils

    private val toInject = listOf(
        ParadoxScriptString::class.java,
        ParadoxScriptNormalParameterArgument::class.java,
    )

    override fun elementsToInjectIn() = toInject

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, host: PsiElement) {
        if (host !is ParadoxLanguageInjectionHost) return // must be `ParadoxLanguageInjectionHost`
        InjectionUtils.enableInjectLanguageAction(host, false) // disable inject language action
        applyInjection(host, registrar)
    }

    private fun applyInjection(host: ParadoxLanguageInjectionHost, registrar: MultiHostRegistrar) {
        if (host.lastChild is PsiErrorElement) return // skip if host has syntax error
        if (applyInjectionForParameterValue(host, registrar)) return
        if (applyInjectionForLocalisationText(host, registrar)) return
    }

    private fun applyInjectionForParameterValue(host: ParadoxLanguageInjectionHost, registrar: MultiHostRegistrar): Boolean {
        val parameterValueInjectionInfos = ParadoxLanguageInjectionService.resolveAllParameterValueInjectionInfo(host)
        if (parameterValueInjectionInfos.isEmpty()) return false
        parameterValueInjectionInfos.forEachFast { injectionInfo ->
            registrar.startInjecting(ParadoxScriptLanguage)
            registrar.addPlace(null, null, host, injectionInfo.rangeInsideHost)
            registrar.doneInjecting()
        }
        return true
    }

    private fun applyInjectionForLocalisationText(host: ParadoxLanguageInjectionHost, registrar: MultiHostRegistrar): Boolean {
        val localisationTextInjectionInfos = ParadoxLanguageInjectionService.resolveAllLocalisationTextInjectionInfo(host)
        if (localisationTextInjectionInfos.isEmpty()) return false
        localisationTextInjectionInfos.forEachFast { injectionInfo ->
            registrar.startInjecting(ParadoxLocalisationLanguage)
            registrar.addPlace("${injectionInfo.localisationName}: \"", "\"", host, injectionInfo.rangeInsideHost)
            registrar.doneInjecting()
        }
        return true
    }
}
