package icu.windea.pls.cwt.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.siblings
import com.intellij.psi.util.startOffset

object CwtPsiService {
    fun canAttachComment(element: PsiElement): Boolean {
        return element is CwtProperty || (element is CwtString && element.isBlockValue())
    }

    fun isMemberContextElement(element: PsiElement): Boolean {
        return element is CwtFile || element.elementType in CwtTokenSets.MEMBER_CONTEXT
    }

    fun collectBetweenBounds(element: CwtBoundMemberContainer, forward: Boolean = true): Sequence<PsiElement>? {
        val leftBound = element.leftBound ?: return null
        val rightBound = element.rightBound ?: return null
        val start = if (forward) leftBound else rightBound
        val end = if (forward) rightBound else leftBound
        return start.siblings(withSelf = false).takeWhile { it != end }
    }

    @Suppress("unused")
    fun isBeforeOrAtBlockLeftBound(element: CwtProperty, offset: Int): Boolean {
        if (offset < element.startOffset) return false
        val block = element.propertyValue<CwtBlock>() ?: return true
        val leftBound = block.leftBound ?: return false
        if (offset > leftBound.endOffset) return false
        return true
    }
}
