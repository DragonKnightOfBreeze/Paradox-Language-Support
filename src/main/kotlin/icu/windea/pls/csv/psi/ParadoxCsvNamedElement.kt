package icu.windea.pls.csv.psi

import com.intellij.psi.*

interface ParadoxCsvNamedElement : PsiNameIdentifierOwner, NavigatablePsiElement {
    override fun getNameIdentifier(): PsiElement? {
        return null
    }
}
