package icu.windea.pls.lang.codeInsight.hints.localisation

import com.intellij.codeInsight.hints.declarative.HintFormat
import com.intellij.codeInsight.hints.declarative.InlayTreeSink
import com.intellij.codeInsight.hints.declarative.InlineInlayPosition
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.endOffset
import icu.windea.pls.core.optimized
import icu.windea.pls.lang.codeInsight.hints.ParadoxHintsProviderNew
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile

class ParadoxLocalisationScriptedVariableReferenceValueHintsProviderNew : ParadoxHintsProviderNew() {
    override fun collectFromElement(element: PsiElement, sink: InlayTreeSink) {
        if (element !is ParadoxScriptedVariableReference) return
        if (element.name.isNullOrEmpty()) return
        val value = element.resolved()?.value ?: return
        val text = "=> ${value}".optimized()

        sink.addPresentation(InlineInlayPosition(element.endOffset, true), hintFormat = HintFormat.default) {
            text(text)
        }
    }

    override fun isSupportedFile(file: PsiFile): Boolean {
        return file is ParadoxLocalisationFile
    }
}
