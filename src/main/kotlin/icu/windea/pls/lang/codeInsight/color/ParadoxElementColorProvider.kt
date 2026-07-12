package icu.windea.pls.lang.codeInsight.color

import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.psi.PsiElement
import icu.windea.pls.ep.codeInsight.color.ParadoxColorProvider
import icu.windea.pls.lang.codeInsight.ParadoxCodeInsightService
import java.awt.Color

/**
 * @see ParadoxColorProvider
 */
class ParadoxElementColorProvider : ElementColorProvider {
    override fun getColorFrom(element: PsiElement): Color? {
        return ParadoxCodeInsightService.getColor(element, fromToken = true)
    }

    override fun setColorTo(element: PsiElement, color: Color) {
        ParadoxCodeInsightService.setColor(element, color, fromToken = true)
    }
}
