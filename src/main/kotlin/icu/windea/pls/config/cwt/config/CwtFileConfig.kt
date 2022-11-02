package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*

data class CwtFileConfig(
	override val pointer: SmartPsiElementPointer<CwtFile>,
	val properties: List<CwtPropertyConfig>,
	val values: List<CwtValueConfig>
) : CwtConfig<CwtFile> {
	companion object {
		val EmptyConfig = CwtFileConfig(emptyPointer(), emptyList(), emptyList())
	}
	
	override val info = CwtConfigInfo()
}

