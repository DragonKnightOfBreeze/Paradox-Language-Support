package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.core.optimized
import icu.windea.pls.lang.codeInsight.hints.ParadoxDeclarativeHintsProvider
import icu.windea.pls.lang.codeInsight.hints.addInlinePresentation
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.lang.psi.select.resolved

/**
 * 通过内嵌提示显示封装变量引用的解析结果（如果可以解析）。默认不启用。
 */
class ParadoxScriptedVariableReferenceValueHintsProvider : ParadoxDeclarativeHintsProvider() {
    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        if (element !is ParadoxScriptedVariableReference) return
        if (element.name.isNullOrEmpty()) return

        val resolved = element.resolved() ?: return
        val value = resolved.value
        sink.addInlinePresentation(element.endOffset) {
            text("=> $value".optimized())
        }
    }
}
