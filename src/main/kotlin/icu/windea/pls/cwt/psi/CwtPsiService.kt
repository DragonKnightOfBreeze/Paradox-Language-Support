package icu.windea.pls.cwt.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.psi.PsiBoundElement
import icu.windea.pls.core.psi.PsiService

@Suppress("unused")
object CwtPsiService {
    fun canAttachComment(element: PsiElement): Boolean {
        return element is CwtProperty || (element is CwtString && element.isBlockValue())
    }

    fun isMemberContextElement(element: PsiElement): Boolean {
        return element is CwtFile || element.elementType in CwtTokenSets.MEMBER_CONTEXT
    }

    fun isBeforeValueLeftBoundEnd(element: CwtProperty, offset: Int): Boolean {
        val value = element.propertyValue?.castOrNull<PsiBoundElement>() ?: return true
        return PsiService.isBeforeLeftBoundEnd(value, offset)
    }

    fun isBeforeBlockLeftBoundEnd(element: CwtProperty, offset: Int): Boolean {
        val block = element.propertyValue?.castOrNull<CwtBlock>() ?: return true
        return PsiService.isBeforeLeftBoundEnd(block, offset)
    }
}
