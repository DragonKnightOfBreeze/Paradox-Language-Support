package icu.windea.pls.script.editor

import com.intellij.openapi.editor.*
import com.intellij.psi.*
import icu.windea.pls.ep.codeInsight.hints.*
import java.awt.*

class ParadoxScriptColorProvider : ElementColorProvider {
    override fun getColorFrom(element: PsiElement): Color? {
        return ParadoxColorProvider.getColor(element, fromToken = true)
    }

    override fun setColorTo(element: PsiElement, color: Color) {
        ParadoxColorProvider.setColor(element, color, fromToken = true)
    }
}
