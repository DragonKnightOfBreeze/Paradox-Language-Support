package icu.windea.pls.cwt.config

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.cwt.psi.*

data class CwtFileConfig(
	override val pointer: SmartPsiElementPointer<CwtFile>, //NOTE 未使用
	val values: List<CwtValueConfig>,
	val properties: List<CwtPropertyConfig>
): CwtConfig<CwtFile> {
	companion object{
		val empty = CwtFileConfig(emptyPointer(), emptyList(), emptyList())
	}
}






