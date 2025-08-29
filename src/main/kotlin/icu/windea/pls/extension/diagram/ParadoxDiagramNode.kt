package icu.windea.pls.extension.diagram

import com.intellij.diagram.PsiDiagramNode
import com.intellij.psi.PsiElement
import icu.windea.pls.extension.diagram.provider.ParadoxDiagramProvider

open class ParadoxDiagramNode(
    element: PsiElement,
    open val provider: ParadoxDiagramProvider
) : PsiDiagramNode<PsiElement>(element, provider) {
    override fun getTooltip(): String? {
        return null
    }
}
