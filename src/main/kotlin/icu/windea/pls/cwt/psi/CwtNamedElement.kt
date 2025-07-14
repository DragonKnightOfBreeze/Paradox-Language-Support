package icu.windea.pls.cwt.psi

import com.intellij.psi.*

interface CwtNamedElement : PsiNameIdentifierOwner, NavigatablePsiElement {
    override fun getNameIdentifier(): PsiElement? {
        return null
    }
}
