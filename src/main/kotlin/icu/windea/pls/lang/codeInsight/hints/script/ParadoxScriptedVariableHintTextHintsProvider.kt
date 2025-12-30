package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProvider
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProviderBase
import icu.windea.pls.lang.codeInsight.PlsCodeInsightService
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsContext
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.codeInsight.hints.addInlinePresentation
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.util.renderers.ParadoxLocalisationTextInlayRenderer
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 封装变量的提示文本的内嵌提示。
 *
 * 来自同名的本地化，或者对应的扩展规则。优先级从低到高。
 *
 * @see ParadoxHintTextProvider
 * @see ParadoxHintTextProviderBase.ScriptedVariable
 */
@Suppress("UnstableApiUsage")
class ParadoxScriptedVariableHintTextHintsProvider : ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("paradox.script.scriptedVariableHintText")

    override val name: String get() = PlsBundle.message("script.hints.scriptedVariableHintText")
    override val description: String get() = PlsBundle.message("script.hints.scriptedVariableHintText.description")
    override val key: SettingsKey<ParadoxHintsSettings> get() = settingsKey

    override val renderLocalisation: Boolean get() = true
    override val renderIcon: Boolean get() = true

    context(context: ParadoxHintsContext)
    override fun collectFromElement(element: PsiElement, sink: InlayHintsSink) {
        // only for scripted variables, not for scripted variable references

        if (element !is ParadoxScriptScriptedVariable) return
        val name = element.name
        if (name.isNullOrEmpty()) return
        if (name.isParameterized()) return

        val hintLocalisation = PlsCodeInsightService.getHintLocalisation(element) ?: return
        val renderer = ParadoxLocalisationTextInlayRenderer(context)
        val presentation = renderer.render(hintLocalisation) ?: return
        sink.addInlinePresentation(element.endOffset) { add(presentation) }
    }
}
