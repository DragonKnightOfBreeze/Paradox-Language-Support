package icu.windea.pls.script.editor

import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.psi.PsiElement
import icu.windea.pls.lang.codeInsight.PlsCodeInsightService
import java.awt.Color

class ParadoxScriptColorProvider : ElementColorProvider {
    override fun getColorFrom(element: PsiElement): Color? {
        return PlsCodeInsightService.getColor(element, fromToken = true)
    }

    override fun setColorTo(element: PsiElement, color: Color) {
        PlsCodeInsightService.setColor(element, color, fromToken = true)
    }
}
