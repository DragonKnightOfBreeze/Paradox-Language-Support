package icu.windea.pls.script.psi

import com.intellij.psi.*
import com.intellij.psi.util.*

object ParadoxScriptPsiUtil {
    fun canAttachComment(element: PsiElement): Boolean {
        return element is ParadoxScriptProperty || (element is ParadoxScriptString && element.isBlockMember())
    }

    fun isMemberContextElement(element: PsiElement): Boolean {
        return element is ParadoxScriptFile || element.elementType in ParadoxScriptTokenSets.MEMBER_CONTEXT
    }
}
