package icu.windea.pls.cwt.codeInsight.surroundWith

import com.intellij.lang.surroundWith.SurroundDescriptor
import com.intellij.lang.surroundWith.Surrounder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.startOffset
import icu.windea.pls.cwt.psi.CwtBoundMemberContainer
import icu.windea.pls.lang.psi.PlsPsiFileManager

// com.intellij.json.surroundWith.JsonSurroundDescriptor
// com.intellij.json.surroundWith.JsonSurrounderBase
// com.intellij.json.surroundWith.JsonWithObjectLiteralSurrounder

class CwtSurroundDescriptor : SurroundDescriptor {
    private val _surrounders = arrayOf(
        CwtPropertySurrounder(),
        CwtBlockSurrounder(),
    )

    override fun getElementsToSurround(file: PsiFile, startOffset: Int, endOffset: Int): Array<out PsiElement> {
        return PlsPsiFileManager.findElementsBetween(file, startOffset, endOffset) { getContainer(it, startOffset, endOffset) }
            .filter { it !is PsiWhiteSpace }
            .toList()
            .toTypedArray<PsiElement>()
    }

    private fun getContainer(element: PsiElement, startOffset: Int, endOffset: Int): CwtBoundMemberContainer? {
        val container = element.parentOfType<CwtBoundMemberContainer>() ?: return null
        if (container.leftBound?.takeIf { it.endOffset <= startOffset } == null) return null
        if (container.rightBound?.takeIf { it.startOffset >= endOffset } == null) return null
        return container
    }

    override fun getSurrounders(): Array<Surrounder> {
        return _surrounders
    }

    override fun isExclusive(): Boolean {
        return false
    }
}
