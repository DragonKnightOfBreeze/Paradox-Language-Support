package com.windea.plugin.idea.pls.core.psi

import com.intellij.psi.*

interface PsiCheckRenameElement : PsiNamedElement {
	fun checkRename() {}
}