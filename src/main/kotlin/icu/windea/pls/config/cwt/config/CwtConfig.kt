package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.*

interface CwtConfig<out T : PsiElement> {
	val pointer: SmartPsiElementPointer<out T>
	
	val configFileName get() = pointer.containingFile?.name ?: anonymousString
}

