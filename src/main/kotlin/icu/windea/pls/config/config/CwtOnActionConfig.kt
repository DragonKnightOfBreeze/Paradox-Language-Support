package icu.windea.pls.config.config

import com.intellij.psi.*

class CwtOnActionConfig(
	override val pointer: SmartPsiElementPointer<out PsiElement>,
	override val info: CwtConfigGroupInfo,
	val config: CwtMemberConfig<*>,
	val name: String,
	val eventType: String
): CwtConfig<PsiElement> //CwtProperty | CwtValue
