package icu.windea.pls.localisation.psi

import com.intellij.psi.*

interface ParadoxLocalisationNamedElement : PsiNameIdentifierOwner, NavigatablePsiElement {
    override fun getNameIdentifier(): PsiElement? {
        return null
    }
}
