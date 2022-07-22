package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.annotations.*
import icu.windea.pls.cwt.psi.*

data class CwtEnumConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name: String,
	val values: Set<@CaseInsensitive String>,
	val valueConfigMap: Map<@CaseInsensitive String, CwtValueConfig>
) : CwtConfig<CwtProperty>
