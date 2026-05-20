package icu.windea.pls.core.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.core.ReadWriteAccess

interface PsiReadWriteAccessAwareElement : PsiElement {
    val readWriteAccess: ReadWriteAccess get() = ReadWriteAccess.ReadWrite
}
