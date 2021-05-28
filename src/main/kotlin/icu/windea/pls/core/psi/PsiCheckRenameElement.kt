package icu.windea.pls.core.psi

import com.intellij.psi.*

interface PsiCheckRenameElement : PsiNamedElement {
	fun checkRename() {}
}