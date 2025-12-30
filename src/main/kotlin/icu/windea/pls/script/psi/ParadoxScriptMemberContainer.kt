package icu.windea.pls.script.psi

import com.intellij.psi.PsiElement

/**
 * @see ParadoxScriptFile
 * @see ParadoxScriptBlockElement
 * @see ParadoxScriptParameterCondition
 */
interface ParadoxScriptMemberContainer: PsiElement {
    val memberList: List<ParadoxScriptMember>
    val propertyList: List<ParadoxScriptProperty>
    val valueList: List<ParadoxScriptValue>
}
