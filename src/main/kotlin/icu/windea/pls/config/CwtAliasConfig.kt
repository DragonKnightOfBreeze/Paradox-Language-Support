package icu.windea.pls.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

data class CwtAliasConfig(
	val name: String,
	val config: CwtConfigProperty,
	override val pointer: SmartPsiElementPointer<CwtProperty>? = null
) : CwtConfig