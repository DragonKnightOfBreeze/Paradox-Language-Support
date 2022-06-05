package icu.windea.pls.config.internal.config

import com.intellij.psi.*
import icu.windea.pls.*

interface InternalConfig : IdAware, IconAware, DescriptionAware {
	val pointer: SmartPsiElementPointer<out PsiElement>
	
	val configFileName get() = pointer.containingFile?.name ?: anonymousString
	val configFileIcon get() = pointer.containingFile?.icon
}