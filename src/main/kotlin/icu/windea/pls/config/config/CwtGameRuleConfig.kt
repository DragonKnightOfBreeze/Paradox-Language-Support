package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

class CwtGameRuleConfig(
	override val pointer: SmartPsiElementPointer<out PsiElement>,
	override val info: CwtConfigGroupInfo,
	val config: CwtMemberConfig<*>,
	val name: String
): CwtConfig<PsiElement> //CwtProperty | CwtValue
