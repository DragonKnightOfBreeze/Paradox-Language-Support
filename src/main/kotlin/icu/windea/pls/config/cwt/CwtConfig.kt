package icu.windea.pls.config.cwt

import com.intellij.psi.*

interface CwtConfig<out T : PsiElement> {
	val pointer: SmartPsiElementPointer<out T>
}

