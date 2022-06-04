package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.annotation.*
import icu.windea.pls.cwt.psi.*

data class CwtScopeGroupConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name: String,
	val values: Set<@CaseInsensitive String>,
	val valueConfigMap: Map<@CaseInsensitive String, CwtValueConfig>
) : CwtConfig<CwtProperty>