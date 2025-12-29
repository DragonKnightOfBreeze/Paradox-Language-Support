package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProvider
import icu.windea.pls.ep.codeInsight.hints.ParadoxHintTextProviderBase
import icu.windea.pls.lang.codeInsight.PlsCodeInsightService
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsContext
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
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

    override fun ParadoxHintsContext.collectFromElement(element: PsiElement, sink: InlayHintsSink): Boolean {
        // only for scripted variables, not for scripted variable references

        if (element !is ParadoxScriptScriptedVariable) return true
        val name = element.name
        if (name.isNullOrEmpty()) return true
        if (name.isParameterized()) return true
        val presentation = collect(element) ?: return true
        val finalPresentation = presentation.toFinalPresentation()
        val endOffset = element.scriptedVariableName.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        return true
    }

    private fun ParadoxHintsContext.collect(element: ParadoxScriptScriptedVariable): InlayPresentation? {
        val hintLocalisation = PlsCodeInsightService.getHintLocalisation(element) ?: return null
        val renderer = ParadoxLocalisationTextInlayRenderer(editor, factory, settings.textLengthLimit, settings.iconHeightLimit)
        return renderer.render(hintLocalisation)
    }
}
