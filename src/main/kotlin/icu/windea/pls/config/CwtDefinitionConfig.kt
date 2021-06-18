package icu.windea.pls.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

data class CwtDefinitionConfig(
	val name: String,
	val propertiesConfig: Map<String, CwtConfigProperty>,
	val subtypePropertiesConfig: Map<String, Map<String, CwtConfigProperty>>,
	override val pointer: SmartPsiElementPointer<CwtProperty>? = null
) : CwtConfig

