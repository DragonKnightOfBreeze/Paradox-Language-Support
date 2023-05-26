package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.cwt.psi.*

/**
 * @property typeKeyFilter (property) type_key_filter: boolean
 * @property typeKeyRegex (option) type_key_regex: string
 * @property startsWith (option) starts_with: string
 * @property pushScope (option) push_scope: scope
 * @property displayName (option) display_name: string
 * @property abbreviation (option) abbreviation: string
 * @property onlyIfNot (option) only_if_not: string[]
 */
class CwtSubtypeConfig(
	override val pointer: SmartPsiElementPointer<out CwtProperty>,
	override val info: CwtConfigGroupInfo,
	val config: CwtPropertyConfig,
	val name: String,
	val typeKeyFilter: ReversibleValue<Set<@CaseInsensitive String>>? = null,
	val typeKeyRegex: Regex? = null,
	val startsWith: @CaseInsensitive String? = null,
	val pushScope: String? = null,
	val displayName: String? = null,
	val abbreviation: String? = null,
	val onlyIfNot: Set<String>? = null
): CwtConfig<CwtProperty>