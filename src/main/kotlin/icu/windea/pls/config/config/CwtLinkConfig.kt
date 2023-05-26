package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.cwt.psi.*

/**
 * @property desc desc: string
 * @property fromData from_data: string
 * @property type type: string
 * @property dataSource data_source: string (expression)
 * @property prefix prefix: string
 * @property forDefinitionType for_definition_type: string
 * @property inputScopes input_scopes | input_scopes: string[]
 * @property outputScope output_scope: string? - 为null时表示会传递scope
 */
class CwtLinkConfig(
	override val pointer: SmartPsiElementPointer<out CwtProperty>,
	override val info: CwtConfigGroupInfo,
	val config: CwtPropertyConfig,
	val name: String,
	val desc: String? = null,
	val fromData: Boolean = false,
	val type: String? = null,
	val dataSource: CwtValueExpression?,
	val prefix: String?,
	val forDefinitionType: String?,
	val inputScopes: Set<String>,
	val outputScope: String?
) : CwtConfig<CwtProperty> {
	override val expression get() = dataSource
}

