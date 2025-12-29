package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.optimized
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProvider
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsSettings
import icu.windea.pls.lang.util.calculators.ParadoxInlineMathCalculator
import icu.windea.pls.script.psi.ParadoxScriptInlineMath

/**
 * 通过内嵌提示显示内联数学表达式的计算结果（如果无需提供额外的传参信息）。
 */
@Deprecated("Use `ParadoxInlineMathResultHintsProviderNew` instead.")
@Suppress("UnstableApiUsage")
class ParadoxInlineMathResultHintsProvider : ParadoxHintsProvider() {
    private val settingsKey = SettingsKey<ParadoxHintsSettings>("paradox.script.inlineMathResult")

    override val name: String get() = PlsBundle.message("script.hints.inlineMathResult")
    override val description: String get() = PlsBundle.message("script.hints.inlineMathResult.description")
    override val key: SettingsKey<ParadoxHintsSettings> get() = settingsKey

    override fun PresentationFactory.collectFromElement(element: PsiElement, file: PsiFile, editor: Editor, settings: ParadoxHintsSettings, sink: InlayHintsSink): Boolean {
        if (element !is ParadoxScriptInlineMath) return true
        if (element.expression.isEmpty()) return true
        val presentation = collect(element) ?: return true
        val finalPresentation = presentation.toFinalPresentation(this, file.project)
        val endOffset = element.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        return true
    }

    private fun PresentationFactory.collect(element: ParadoxScriptInlineMath): InlayPresentation? {
        val calculator = ParadoxInlineMathCalculator()
        val result = runCatchingCancelable { calculator.calculate(element) }.getOrNull() ?: return null
        return smallText("=> ${result.formatted()}".optimized())
    }
}
