package icu.windea.pls.script.psi

import com.intellij.psi.PsiElement

interface ParadoxScriptMemberContainer: PsiElement {
    val memberList: List<ParadoxScriptMemberElement>
    val propertyList: List<ParadoxScriptProperty>
    val valueList: List<ParadoxScriptValue>
}
