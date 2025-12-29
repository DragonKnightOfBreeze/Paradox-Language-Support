package icu.windea.pls.lang.codeInsight.hints.localisation

import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.core.optimized
import icu.windea.pls.lang.codeInsight.hints.ParadoxDeclarativeHintsProvider
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference

/**
 * 通过内嵌提示显示封装变量引用的解析结果（如果可以解析）。默认不启用。
 */
class ParadoxLocalisationScriptedVariableReferenceValueHintsProviderNew : ParadoxDeclarativeHintsProvider() {
    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        if (element !is ParadoxScriptedVariableReference) return
        if (element.name.isNullOrEmpty()) return
        val value = element.resolved()?.value ?: return

        sink.addInlinePresentation(element.endOffset) {
            val text = "=> $value".optimized()
            text(text)
        }
    }
}
