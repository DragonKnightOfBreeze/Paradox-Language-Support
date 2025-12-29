package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.optimized
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsContext
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.util.evaluators.ParadoxInlineMathEvaluator
import icu.windea.pls.script.psi.ParadoxScriptInlineMath

/**
 * 通过内嵌提示显示内联数学表达式的求值结果（如果无需提供额外的传参信息）。
 */
@Deprecated("Use `ParadoxInlineMathResultHintsProviderNew` instead.")
@Suppress("UnstableApiUsage")
class ParadoxInlineMathResultHintsProvider : ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("paradox.script.inlineMathResult")

    override val name: String get() = PlsBundle.message("script.hints.inlineMathResult")
    override val description: String get() = PlsBundle.message("script.hints.inlineMathResult.description")
    override val key: SettingsKey<ParadoxHintsSettings> get() = settingsKey

    context(context: ParadoxHintsContext)
    override fun collectFromElement(element: PsiElement, sink: InlayHintsSink): Boolean {
        if (element !is ParadoxScriptInlineMath) return true
        if (element.expression.isEmpty()) return true
        val presentation = collect(element) ?: return true
        val finalPresentation = presentation.toFinalPresentation()
        val endOffset = element.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        return true
    }

    context(context: ParadoxHintsContext)
    private fun collect(element: ParadoxScriptInlineMath): InlayPresentation? {
        val evaluator = ParadoxInlineMathEvaluator()
        val result = context.runCatchingCancelable { evaluator.evaluate(element) }.getOrNull() ?: return null
        val text = "=> ${result.formatted()}".optimized()
        return context.factory.smallText(text)
    }
}
