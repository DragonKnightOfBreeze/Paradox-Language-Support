package icu.windea.pls.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

interface CwtConfig<out T : PsiElement> {
	val pointer: SmartPsiElementPointer<out T>
}