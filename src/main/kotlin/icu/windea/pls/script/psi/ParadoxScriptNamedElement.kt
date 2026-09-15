package icu.windea.pls.script.psi

import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNameIdentifierOwner
import com.intellij.psi.PsiNamedElement

interface ParadoxScriptNamedElement : PsiNamedElement, PsiNameIdentifierOwner, NavigatablePsiElement {
    override fun getNameIdentifier(): PsiElement? {
        return null
    }
}
