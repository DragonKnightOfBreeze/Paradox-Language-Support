package icu.windea.pls.model.constraints

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubIndexKey

interface ParadoxIndexConstraint<T : PsiElement> {
    val indexKey: StubIndexKey<String, T>
    val ignoreCase: Boolean
    val inferred: Boolean
}
