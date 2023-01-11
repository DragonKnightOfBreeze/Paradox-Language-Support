package icu.windea.pls.core.psi

import com.intellij.psi.*

interface PsiNodeReference : PsiReference, PsiNode {
	fun resolve(exact: Boolean): PsiElement?
	
	fun canResolve(): Boolean {
		return resolve(false) != null
	}
}

fun PsiNodeReference?.canResolve() = this == null || this.canResolve()