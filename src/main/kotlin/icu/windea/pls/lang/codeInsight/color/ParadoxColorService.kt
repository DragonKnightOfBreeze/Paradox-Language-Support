package icu.windea.pls.lang.codeInsight.color

import com.intellij.psi.PsiElement
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.orNull
import icu.windea.pls.ep.codeInsight.color.ParadoxColorProvider
import java.awt.Color

object ParadoxColorService {
    /**
     * @see ParadoxColorProvider.getColor
     */
    fun getColor(element: PsiElement, fromToken: Boolean = false): Color? {
        ParadoxColorProvider.EP_NAME.extensionList.forEachFast f@{ ep ->
            val targetElement = if (fromToken) ep.getTargetElement(element) else element
            if (targetElement == null) return@f
            ep.getColor(targetElement)?.let { return it }
        }
        return null
    }

    /**
     * @see ParadoxColorProvider.setColor
     */
    fun setColor(element: PsiElement, color: Color, fromToken: Boolean = false): Boolean {
        ParadoxColorProvider.EP_NAME.extensionList.forEachFast f@{ ep ->
            val targetElement = if (fromToken) ep.getTargetElement(element) else element
            if (targetElement == null) return@f
            ep.setColor(targetElement, color).orNull()?.let { return it }
        }
        return false
    }
}
