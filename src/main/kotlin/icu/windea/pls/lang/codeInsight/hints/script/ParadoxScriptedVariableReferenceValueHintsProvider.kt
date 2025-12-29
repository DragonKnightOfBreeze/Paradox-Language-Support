package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.optimized
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsContext
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference

/**
 * 通过内嵌提示显示封装变量引用的解析结果（如果可以解析）。默认不启用。
 */
@Deprecated("Use `ParadoxScriptedVariableReferenceValueHintsProviderNew` instead.")
@Suppress("UnstableApiUsage")
class ParadoxScriptedVariableReferenceValueHintsProvider : ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("paradox.script.scriptedVariableReferenceValue")

    override val name: String get() = PlsBundle.message("script.hints.scriptedVariableReferenceValue")
    override val description: String get() = PlsBundle.message("script.hints.scriptedVariableReferenceValue.description")
    override val key: SettingsKey<ParadoxHintsSettings> get() = settingsKey

    context(context: ParadoxHintsContext)
    override fun collectFromElement(element: PsiElement, sink: InlayHintsSink): Boolean {
        if (element !is ParadoxScriptedVariableReference) return true
        if (element.name.isNullOrEmpty()) return true
        val presentation = collect(element) ?: return true
        val finalPresentation = presentation.toFinalPresentation()
        val endOffset = element.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        return true
    }

    context(context: ParadoxHintsContext)
    private fun collect(element: ParadoxScriptedVariableReference): InlayPresentation? {
        val value = element.resolved()?.value ?: return null
        return context.factory.smallText("=> $value".optimized())
    }
}
