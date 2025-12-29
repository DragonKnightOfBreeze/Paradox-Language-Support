package icu.windea.pls.lang.codeInsight.hints.script

import com.intellij.codeInsight.hints.declarative.HintFormat
import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.codeInsight.hints.declarative.InlineInlayPosition
import com.intellij.psi.PsiElement
import com.intellij.psi.util.endOffset
import icu.windea.pls.core.optimized
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProviderNew
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isExpression

class ParadoxDynamicValueInfoHintsProviderNew : ParadoxHintsProviderNew() {
    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        // ignored for value_field or variable_field or other variants

        if (element !is ParadoxScriptStringExpressionElement) return
        if (!element.isExpression()) return
        val expression = element.name
        if (expression.isEmpty()) return
        if (expression.isParameterized()) return

        val resolveConstraint = ParadoxResolveConstraint.DynamicValueStrictly
        val resolved = element.references.reversed().filter { resolveConstraint.canResolve(it) }.firstNotNullOfOrNull { it.resolve() }
        if (resolved !is ParadoxDynamicValueElement) return

        val name = resolved.name
        if (name.isEmpty()) return
        if (name.isParameterized()) return
        val type = resolved.dynamicValueType
        val text = ": $type".optimized()

        sink.addPresentation(InlineInlayPosition(element.endOffset, true), hintFormat = HintFormat.default) {
            text(text)
        }
    }

    override fun isSupportedFile(file: com.intellij.psi.PsiFile): Boolean {
        return file is ParadoxScriptFile
    }
}
