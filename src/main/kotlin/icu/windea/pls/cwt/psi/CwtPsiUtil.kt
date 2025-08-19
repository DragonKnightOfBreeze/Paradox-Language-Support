package icu.windea.pls.cwt.psi

import com.intellij.psi.*
import com.intellij.psi.util.*

object CwtPsiUtil {
    fun canAttachComment(element: PsiElement): Boolean {
        return element is CwtProperty || (element is CwtString && element.isBlockValue())
    }

    fun isMemberContextElement(element: PsiElement): Boolean {
        return element is CwtFile || element.elementType in CwtTokenSets.MEMBER_CONTEXT
    }
}
