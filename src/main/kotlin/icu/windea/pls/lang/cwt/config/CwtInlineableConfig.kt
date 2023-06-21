package icu.windea.pls.lang.cwt.config

import com.intellij.psi.*

interface CwtInlineableConfig<out T: PsiElement>: CwtConfig<T> {
	val name: String
	val config: CwtMemberConfig<T>
}