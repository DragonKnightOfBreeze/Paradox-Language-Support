package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

data class CwtLocalisationCommandConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name: String,
	val supportedScopes: Set<String>
) : CwtConfig<CwtProperty>