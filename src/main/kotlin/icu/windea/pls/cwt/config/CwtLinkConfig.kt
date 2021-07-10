package icu.windea.pls.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.expression.*
import icu.windea.pls.cwt.psi.*

/**
 * @property desc desc: string
 * @property fromData from_data: string
 * @property type type: string
 * @property dataSource data_source: string (expression)
 * @property prefix prefix: string
 * @property inputScopes input_scopes | input_scopes: string[]
 * @property outputScope output_scope: string
 */
data class CwtLinkConfig(
	override val pointer: SmartPsiElementPointer<CwtProperty>,
	val name:String,
	val desc:String? = null,
	val fromData:Boolean = false,
	val type:String? = null,
	val dataSource:CwtValueExpression? = null,
	val prefix:String? = null,
	val inputScopes:List<String>,
	val outputScope:String,
): CwtConfig<CwtProperty>

