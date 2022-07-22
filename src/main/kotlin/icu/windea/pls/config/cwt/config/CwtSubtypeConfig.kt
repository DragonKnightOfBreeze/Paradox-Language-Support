package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.annotations.*
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
	val name: String,
	val config: CwtPropertyConfig,
	val typeKeyFilter: ReversibleSet<@CaseInsensitive String>? = null,
	val pushScope: String? = null,
	val startsWith: @CaseInsensitive String? = null,
	val displayName: String? = null,
	val abbreviation: String? = null,
	val onlyIfNot: Set<String>? = null
): CwtConfig<CwtProperty>