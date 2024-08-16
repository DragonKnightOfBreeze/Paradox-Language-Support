package icu.windea.pls.config.config

import com.intellij.psi.*

sealed interface CwtInlineableConfig<out T : PsiElement, out C : CwtConfig<T>> : CwtDelegatedConfig<T, C>
