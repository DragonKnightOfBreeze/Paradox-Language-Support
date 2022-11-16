package icu.windea.pls.core.psi

import com.intellij.openapi.editor.colors.*
import com.intellij.psi.*

interface PsiSmartReference : PsiReference {
	fun resolve(exact: Boolean): PsiElement?
	
	fun resolved(): Boolean {
		return resolve(false) != null
	}
	
	fun resolveTextAttributesKey(): TextAttributesKey? {
		return null
	}
}

fun PsiSmartReference?.resolved() = this == null || this.resolved()
