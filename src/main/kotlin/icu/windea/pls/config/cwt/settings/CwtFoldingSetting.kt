package icu.windea.pls.config.cwt.settings

import com.intellij.psi.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.cwt.psi.*

class CwtFoldingSetting(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val info: CwtConfigInfo,
	val key: String?,
	val keys: Set<String>?,
	val placeholder: String
) : CwtConfig<CwtProperty>
