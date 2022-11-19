package icu.windea.pls.core.psi

import com.intellij.openapi.editor.colors.*
import com.intellij.psi.*

interface SmartPsiReference : PsiReference {
	fun resolve(exact: Boolean): PsiElement?
	
	fun canResolve(): Boolean {
		return resolve(false) != null
	}
	
	fun resolveTextAttributesKey(): TextAttributesKey? {
		return null
	}
}

fun SmartPsiReference?.canResolve() = this == null || this.canResolve()
