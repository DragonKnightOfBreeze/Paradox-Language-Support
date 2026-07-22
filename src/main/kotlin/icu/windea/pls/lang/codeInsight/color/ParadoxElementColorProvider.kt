package icu.windea.pls.lang.codeInsight.color

import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.psi.PsiElement
import icu.windea.pls.ep.codeInsight.color.ParadoxColorProvider
import java.awt.Color

/**
 * @see ParadoxColorProvider
 */
class ParadoxElementColorProvider : ElementColorProvider {
    override fun getColorFrom(element: PsiElement): Color? {
        return ParadoxColorService.getColor(element, fromToken = true)
    }

    override fun setColorTo(element: PsiElement, color: Color) {
        ParadoxColorService.setColor(element, color, fromToken = true)
    }
}
