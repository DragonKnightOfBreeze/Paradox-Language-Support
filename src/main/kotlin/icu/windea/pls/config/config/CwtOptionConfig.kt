package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.model.*

data class CwtOptionConfig(
	override val pointer: SmartPsiElementPointer<CwtOption>, //NOTE 目前并未使用，因此直接传入emptyPointer()就行
	override val info: CwtConfigGroupInfo,
	override val key: String,
	override val value: String,
	override val valueTypeId: Byte = CwtType.String.id,
	override val separatorTypeId: Byte = CwtSeparatorType.EQUAL.id,
	override val options: List<CwtOptionConfig>? = null,
	override val optionValues: List<CwtOptionValueConfig>? = null
) : CwtConfig<CwtOption>, CwtPropertyAware, CwtOptionsAware