package icu.windea.pls.script.psi

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.util.elementType
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.siblings
import com.intellij.psi.util.startOffset
import icu.windea.pls.core.castOrNull

object ParadoxScriptPsiService {
    fun canAttachComment(element: PsiElement): Boolean {
        return element is ParadoxScriptProperty || (element is ParadoxScriptString && element.isBlockMember())
    }

    fun isMemberContextElement(element: PsiElement): Boolean {
        return element is ParadoxScriptFile || element.elementType in ParadoxScriptTokenSets.MEMBER_CONTEXT
    }

    fun collectBetweenBounds(element: ParadoxScriptBoundMemberContainer, forward: Boolean = true): Sequence<PsiElement>? {
        val leftBound = element.leftBound ?: return null
        val rightBound = element.rightBound ?: return null
        val start = if (forward) leftBound else rightBound
        val end = if (forward) rightBound else leftBound
        return start.siblings(withSelf = false).takeWhile { it != end }
    }

    fun isBeforeOrAtBlockLeftBound(element: ParadoxScriptProperty, offset: Int): Boolean {
        if (offset < element.startOffset) return false
        val block = element.propertyValue<ParadoxScriptBlock>() ?: return true
        val leftBound = block.leftBound ?: return false
        if (offset > leftBound.endOffset) return false
        return true
    }
}
