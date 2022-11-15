package icu.windea.pls.core.psi

import com.intellij.openapi.editor.colors.*
import com.intellij.psi.*

interface PsiAnnotatedReference : PsiReference {
	fun resolveTextAttributesKey(): TextAttributesKey?
}
