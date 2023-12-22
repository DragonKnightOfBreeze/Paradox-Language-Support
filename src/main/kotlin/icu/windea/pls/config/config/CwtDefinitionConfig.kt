package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

class CwtDefinitionConfig(
	override val pointer: SmartPsiElementPointer<out PsiElement>,
	override val info: CwtConfigGroupInfo,
	val config: CwtMemberConfig<*>,
	val name: String,
	val type: String
): CwtConfig<PsiElement> //CwtProperty | CwtValue

class CwtParameterConfig(
	override val pointer: SmartPsiElementPointer<out PsiElement>,
	override val info: CwtConfigGroupInfo,
	val config: CwtMemberConfig<*>,
	val name: String,
	val contextKey: String
): CwtConfig<PsiElement> //CwtProperty | CwtValue