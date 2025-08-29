package icu.windea.pls.core.psi

import com.intellij.psi.PsiReference

interface PsiReferencesAware {
    fun getReferences(): Array<out PsiReference>? = null
}
