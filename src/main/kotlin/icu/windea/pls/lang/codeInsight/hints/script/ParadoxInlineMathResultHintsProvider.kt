@file:Suppress("UnstableApiUsage")

package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.NoSettings
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
import icu.windea.pls.lang.util.calculators.ParadoxInlineMathCalculator
import icu.windea.pls.script.psi.ParadoxScriptInlineMath

/**
 * 通过内嵌提示显示静态的内联数学表达式的计算结果。
 */
class ParadoxInlineMathResultHintsProvider : ParadoxScriptHintsProvider<NoSettings>() {
    private val settingsKey = SettingsKey<NoSettings>("ParadoxInlineMathResultHintsSettingsKey")

    override val name: String get() = PlsBundle.message("script.hints.inlineMathResult")
    override val description: String get() = PlsBundle.message("script.hints.inlineMathResult.description")
    override val key: SettingsKey<NoSettings> get() = settingsKey

    override fun createSettings() = NoSettings()

    override fun PresentationFactory.collect(element: PsiElement, file: PsiFile, editor: Editor, settings: NoSettings, sink: InlayHintsSink): Boolean {
        if (element !is ParadoxScriptInlineMath) return true
        if (element.expression.isEmpty()) return true
        val presentation = doCollect(element) ?: return true
        val finalPresentation = presentation.toFinalPresentation(this, file.project)
        val endOffset = element.endOffset
        sink.addInlineElement(endOffset, true, finalPresentation, false)
        return true
    }

    private fun PresentationFactory.doCollect(element: ParadoxScriptInlineMath): InlayPresentation? {
        val calculator = ParadoxInlineMathCalculator()
        val result = runCatchingCancelable { calculator.calculate(element) }.getOrNull() ?: return null
        return smallText("=> ${result.formatted()}".optimized())
    }
}
