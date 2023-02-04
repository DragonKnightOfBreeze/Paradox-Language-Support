package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*

/**
 * @property desc desc: string
 * @property fromData from_data: string
 * @property type type: string
 * @property dataSource data_source: string (expression)
 * @property prefix prefix: string
 * @property forDefinitionType for_definition_type: string
 * @property inputScopes input_scopes | input_scopes: string[]
 * @property outputScope output_scope: string
 * @property transferScope 是否传递scope
 */
data class CwtLinkConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
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
	val outputScope: String,
	val transferScope: Boolean
) : CwtConfig<CwtProperty> {
	val inputAnyScope get() = inputScopes == ParadoxScopeHandler.anyScopeIdSet
	val outputAnyScope get() = outputScope == ParadoxScopeHandler.anyScopeId
	
	override val expression get() = dataSource
}

