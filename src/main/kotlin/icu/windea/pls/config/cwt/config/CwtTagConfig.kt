package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

/**
 * @param since (property) string
 * @param supportedTypes (property) string[]
 */
data class CwtTagConfig(
	override val pointer: SmartPsiElementPointer<out CwtProperty>,
	override val info: CwtConfigGroupInfo,
	val name: String,
	val since: String?,
	val supportedTypes: Set<String>
): CwtConfig<CwtProperty>