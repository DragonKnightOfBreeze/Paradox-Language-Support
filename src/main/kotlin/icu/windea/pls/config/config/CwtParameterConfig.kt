package icu.windea.pls.config.config

import com.intellij.psi.*

class CwtParameterConfig(
	override val pointer: SmartPsiElementPointer<out PsiElement>,
	override val info: CwtConfigGroupInfo,
	val config: CwtMemberConfig<*>,
	val name: String,
	val contextKey: String
): CwtConfig<PsiElement> //CwtProperty | CwtValue
