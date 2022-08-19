package icu.windea.pls.config.internal.config

import com.intellij.psi.*
import javax.swing.*

interface InternalConfig {
	val pointer: SmartPsiElementPointer<out PsiElement>
	val id: String
	val description: String
	val icon: Icon
	
	//val configFileName get() = pointer.containingFile?.name ?: anonymousString
	//val configFileIcon get() = pointer.containingFile?.icon
}