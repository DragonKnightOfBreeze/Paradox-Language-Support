package icu.windea.pls.core.psi

import com.intellij.psi.*

interface PsiReferencesAware {
    fun getReferences(): Array<out PsiReference>? = null
}