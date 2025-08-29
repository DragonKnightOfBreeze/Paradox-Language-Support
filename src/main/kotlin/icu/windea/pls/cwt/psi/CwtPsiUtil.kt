package icu.windea.pls.cwt.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType

object CwtPsiUtil {
    fun canAttachComment(element: PsiElement): Boolean {
        return element is CwtProperty || (element is CwtString && element.isBlockValue())
    }

    fun isMemberContextElement(element: PsiElement): Boolean {
        return element is CwtFile || element.elementType in CwtTokenSets.MEMBER_CONTEXT
    }
}
