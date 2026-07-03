package icu.windea.pls.script.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.psi.PsiBoundElement
import icu.windea.pls.core.psi.PsiService

@Suppress("unused")
object ParadoxScriptPsiService {
    fun canAttachComment(element: PsiElement): Boolean {
        return element is ParadoxScriptProperty || (element is ParadoxScriptString && element.isDirectValue())
    }

    fun isLenientMemberContext(element: PsiElement): Boolean {
        return element is ParadoxScriptMemberContext
    }

    fun isStrictMemberContext(element: PsiElement): Boolean {
        return element is ParadoxScriptFile || element.elementType in ParadoxScriptTokenSets.MEMBER_CONTEXT_TOKENS
    }

    fun isPropertySeparator(element: PsiElement): Boolean {
        return element.elementType in ParadoxScriptTokenSets.PROPERTY_SEPARATOR_TOKENS
    }

    fun isBeforeValueLeftBoundEnd(element: ParadoxScriptProperty, offset: Int): Boolean {
        val value = element.propertyValue?.castOrNull<PsiBoundElement>() ?: return true
        return PsiService.isBeforeLeftBoundEnd(value, offset)
    }

    fun isBeforeBlockLeftBoundEnd(element: ParadoxScriptProperty, offset: Int): Boolean {
        val block = element.propertyValue?.castOrNull<ParadoxScriptBlock>() ?: return true
        return PsiService.isBeforeLeftBoundEnd(block, offset)
    }
}
