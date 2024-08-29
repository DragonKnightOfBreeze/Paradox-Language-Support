package icu.windea.pls.script.psi

import com.intellij.psi.*

interface ParadoxScriptNamedElement : PsiNameIdentifierOwner, NavigatablePsiElement {
    override fun getNameIdentifier(): PsiElement? {
        return null
    }
}
