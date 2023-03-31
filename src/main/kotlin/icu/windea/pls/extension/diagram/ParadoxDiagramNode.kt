package icu.windea.pls.extension.diagram

import com.intellij.diagram.*
import com.intellij.psi.*
import icu.windea.pls.extension.diagram.provider.*

open class ParadoxDiagramNode(
    element: PsiElement,
    open val provider: ParadoxDiagramProvider
): PsiDiagramNode<PsiElement>(element, provider) {
    override fun getIdentifyingElement(): PsiElement {
        return super.getIdentifyingElement()
    }
    
    override open fun getTooltip(): String? {
        return null
    }
}
