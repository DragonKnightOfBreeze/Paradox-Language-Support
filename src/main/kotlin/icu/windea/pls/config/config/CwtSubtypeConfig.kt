package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*

/**
 * @property typeKeyFilter (option) type_key_filter: string | string[]
 * @property pushScope (option) push_scope: scope
 * @property startsWith (option) starts_with: string
 * @property displayName (option) display_name: string
 * @property abbreviation (option) abbreviation: string
 * @property onlyIfNot (option) only_if_not: string[]
 */
data class CwtSubtypeConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	override val info: CwtConfigGroupInfo,
	val config: CwtPropertyConfig,
	val name: String,
	val typeKeyFilter: ReversibleSet<@CaseInsensitive String>? = null,
	val pushScope: String? = null,
	val startsWith: @CaseInsensitive String? = null,
	val displayName: String? = null,
	val abbreviation: String? = null,
	val onlyIfNot: Set<String>? = null
): CwtConfig<CwtProperty>