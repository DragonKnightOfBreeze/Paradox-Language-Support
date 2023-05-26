package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

class CwtLocalisationCommandConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val info: CwtConfigGroupInfo,
	val name: String,
	val supportedScopes: Set<String>
) : CwtConfig<CwtProperty>