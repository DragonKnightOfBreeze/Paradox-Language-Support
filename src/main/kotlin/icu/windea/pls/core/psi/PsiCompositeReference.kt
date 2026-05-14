package icu.windea.pls.core.psi

import com.intellij.psi.PsiReference

interface PsiCompositeReference: PsiReference {
    fun getReferences(): Array<out PsiReference>? = null
}
