package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.config.script.*
import icu.windea.pls.cwt.psi.*

data class CwtLocalisationCommandConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val info: CwtConfigGroupInfo,
	val name: String,
	val supportedScopes: Set<String>
) : CwtConfig<CwtProperty> {
	val supportAnyScope = supportedScopes == ScopeConfigHandler.anyScopeIdSet
}