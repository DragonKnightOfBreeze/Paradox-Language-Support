package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.declarative.HintFormat
import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.codeInsight.hints.declarative.InlineInlayPosition
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.core.optimized
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProviderNew
import icu.windea.pls.lang.util.calculators.ParadoxInlineMathCalculator
import icu.windea.pls.script.psi.ParadoxScriptInlineMath

class ParadoxInlineMathResultHintsProviderNew: ParadoxHintsProviderNew() {
    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        if (element !is ParadoxScriptInlineMath) return
        if (element.expression.isEmpty()) return
        collect(element, sink)
    }

    private fun collect(element: ParadoxScriptInlineMath, sink: InlayTreeSink) {
        val calculator = ParadoxInlineMathCalculator()
        val result = runCatchingCancelable { calculator.calculate(element) }.getOrNull() ?: return
        val text = "=> ${result.formatted()}".optimized()
        sink.addPresentation(InlineInlayPosition(element.endOffset, true), hintFormat = HintFormat.default) {
            text(text)
        }
    }
}
