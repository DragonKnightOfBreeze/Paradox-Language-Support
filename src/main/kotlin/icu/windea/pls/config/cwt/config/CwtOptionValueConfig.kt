package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

data class CwtOptionValueConfig(
	override val pointer: SmartPsiElementPointer<CwtValue>, //NOTE 未使用
	override val info: CwtConfigInfo,
	val value: String,
	val booleanValue: Boolean? = null,
	val intValue: Int? = null,
	val floatValue: Float? = null,
	val stringValue: String? = null,
	val options: List<CwtOptionConfig>? = null,
	val optionValues: List<CwtOptionValueConfig>? = null
) : CwtConfig<CwtValue>