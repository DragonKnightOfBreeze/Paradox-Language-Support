package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.config.core.*
import icu.windea.pls.config.core.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*

sealed class CwtDataConfig<out T : PsiElement> : CwtConfig<T> {
	abstract val value: String
	abstract val booleanValue: Boolean?
	abstract val intValue: Int?
	abstract val floatValue: Float?
	abstract val stringValue: String?
	abstract val configs: List<CwtDataConfig<*>>?
	abstract val documentation: String?
	abstract val options: List<CwtOptionConfig>?
	abstract val optionValues: List<CwtOptionValueConfig>?
	
	abstract override val expression: CwtDataExpression
	
	@Volatile var parent: CwtDataConfig<*>? = null
	
	val isBlock: Boolean get() = configs != null
	val values: List<CwtValueConfig>? by lazy { configs?.filterIsInstance<CwtValueConfig>() }
	val properties: List<CwtPropertyConfig>? by lazy { configs?.filterIsInstance<CwtPropertyConfig>() }
	
	override fun resolved(): CwtDataConfig<*> = this
	
	override fun resolvedOrNull(): CwtDataConfig<*>? = null
	
	//may on:
	// * a config expression in declaration config
	// * a config expression in subtype structure config
	val cardinality by lazy {
		val option = options?.find { it.key == "cardinality" }
		if(option == null) {
			//如果没有注明且类型是常量，则推断为 1..1
			if(expression.type.isConstant()) {
				return@lazy CwtCardinalityExpression.resolve("1..1")
			}
		}
		option?.stringValue?.let { s -> CwtCardinalityExpression.resolve(s) }
	}
	
	//may on:
	// * a config expression in declaration config (include root expression, e.g. "army = { ... }")
	// * a type config (e.g. "type[xxx] = { ... }")
	// * a subtype config (e.g. "subtype[xxx] = { ... }")
	val replaceScope by lazy {
		val option = options?.find { it.key == "replace_scope" || it.key == "replace_scopes" }
		if(option == null) return@lazy null
		val options = option.options ?: return@lazy null
		val map = options.associateBy({ it.key.lowercase() }, { it.stringValue?.let { v -> ParadoxScopeHandler.getScopeId(v) } })
		ParadoxScopeContext.resolve(map)
	}
	
	//may on:
	// * a config expression in declaration config (include root expression, e.g. "army = { ... }")
	// * a type config (e.g. "type[xxx] = { ... }")
	// * a subtype config (e.g. "subtype[xxx] = { ... }")
	val pushScope by lazy {
		val option = options?.find { it.key == "push_scope" }
		option?.stringValue?.let { v -> ParadoxScopeHandler.getScopeId(v) }
	}
	
	//may on:
	// * a config expression in declaration config
	val supportedScopes by lazy {
		val option = options?.find { it.key == "scope" || it.key == "scopes" }
		buildSet {
			option?.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) }
			option?.optionValues?.forEach { it.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) } }
		}.ifEmpty { ParadoxScopeHandler.anyScopeIdSet }
	}
	
	val supportAnyScope = supportedScopes == ParadoxScopeHandler.anyScopeIdSet
	
	/**
	 * 深拷贝。
	 */
	fun deepCopyConfigs(): List<CwtDataConfig<*>>? {
		return configs?.map { config ->
			when(config) {
				is CwtPropertyConfig -> config.copy(configs = config.deepCopyConfigs())
				is CwtValueConfig -> config.copy(configs = config.deepCopyConfigs())
			}.also { it.parent = this }
		}
	}
	
	/**
	 * 深拷贝 + 根据定义的名字、类型、子类型进行合并。
	 */
	fun deepMergeConfigs(name: String?, type: String, subtypes: List<String>?, configGroup: CwtConfigGroup): List<CwtDataConfig<*>> {
		val mergedConfigs: MutableList<CwtDataConfig<*>>? = if(configs != null) SmartList() else null
		configs?.forEach { config ->
			val childConfigList = config.deepMergeConfigs(name, type, subtypes, configGroup)
			if(childConfigList.isNotEmpty()) {
				for(childConfig in childConfigList) {
					mergedConfigs?.add(childConfig)
				}
			}
		}
		when(this) {
			is CwtValueConfig -> {
				val valueExpression = CwtConfigExpressionHandler.handle(value, name, type, subtypes, configGroup)
				val mergedConfig = copy(value = valueExpression, configs = mergedConfigs)
				return mergedConfig.also { parent = it.parent }.toSingletonList()
			}
			is CwtPropertyConfig -> {
				val subtypeName = key.removeSurroundingOrNull("subtype[", "]")
				if(subtypeName == null) {
					val keyExpression = CwtConfigExpressionHandler.handle(key, name, type, subtypes, configGroup)
					val valueExpression = CwtConfigExpressionHandler.handle(value, name, type, subtypes, configGroup)
					val mergedConfig = copy(key = keyExpression, value = valueExpression, configs = mergedConfigs)
					return mergedConfig.also { parent = it.parent }.toSingletonList()
				} else if(matchesDefinitionSubtypeExpression(subtypeName, subtypes)) {
					return mergedConfigs.orEmpty()
				} else {
					return emptyList()
				}
			}
		}
	}
}
