package icu.windea.pls.script.psi

import com.intellij.psi.PsiElement

/**
 * @see ParadoxScriptRootBlock
 * @see ParadoxScriptBlock
 * @see ParadoxScriptParameterCondition
 */
interface ParadoxScriptMemberContainer: PsiElement {
    val memberList: List<ParadoxScriptMemberElement>
    val propertyList: List<ParadoxScriptProperty>
    val valueList: List<ParadoxScriptValue>
}
