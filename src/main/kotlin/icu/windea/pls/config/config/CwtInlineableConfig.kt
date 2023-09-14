package icu.windea.pls.config.config

import com.intellij.psi.*

interface CwtInlineableConfig<out T: PsiElement>: CwtConfig<T> {
	val name: String
	val config: CwtMemberConfig<T>
}