package icu.windea.pls.cwt.psi

import com.intellij.psi.PsiElement

/**
 * @see CwtRootBlock
 * @see CwtBlock
 */
interface CwtMemberContainer: PsiElement {
    val memberList: List<CwtMember>
    val propertyList: List<CwtProperty>
    val valueList: List<CwtValue>
}
