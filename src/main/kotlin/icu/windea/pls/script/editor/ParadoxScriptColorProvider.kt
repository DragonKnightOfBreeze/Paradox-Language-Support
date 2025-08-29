package icu.windea.pls.script.editor

import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.psi.PsiElement
import icu.windea.pls.ep.codeInsight.hints.ParadoxColorProvider
import java.awt.Color

class ParadoxScriptColorProvider : ElementColorProvider {
    override fun getColorFrom(element: PsiElement): Color? {
        return ParadoxColorProvider.getColor(element, fromToken = true)
    }

    override fun setColorTo(element: PsiElement, color: Color) {
        ParadoxColorProvider.setColor(element, color, fromToken = true)
    }
}
