package icu.windea.pls.config.config

import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.config.expression.CwtDataType.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.model.*

sealed class CwtDataConfig<out T : PsiElement> : UserDataHolderBase(), CwtConfig<T> {
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
	
	var inlineableConfig: CwtInlineableConfig<@UnsafeVariance T>? = null
	
	override fun resolved(): CwtDataConfig<T> = inlineableConfig?.config?.castOrNull<CwtDataConfig<T>>() ?: this
	
	override fun resolvedOrNull(): CwtDataConfig<T>? = inlineableConfig?.config?.castOrNull<CwtDataConfig<T>>()
	
	override fun toString(): String {
		return super.toString()
	}
	
	/**
	 * 深拷贝。
	 */
	fun deepCopyConfigs(): List<CwtDataConfig<*>>? {
		return configs?.map { config ->
			when(config) {
				is CwtPropertyConfig -> config.copy(configs = config.deepCopyConfigs()).also { it.parent = config.parent }
				is CwtValueConfig -> config.copy(configs = config.deepCopyConfigs()).also { it.parent = config.parent }
			}
		}
	}
	
	/**
	 * 深拷贝 + 根据定义的名字、类型、子类型进行合并。
	 */
	fun deepMergeConfigs(configContext: CwtConfigContext): List<CwtDataConfig<*>> {
		//因为之后可能需要对得到的声明规则进行注入，需要保证当注入式所有规则列表都是可变的
		
		val mergedConfigs: MutableList<CwtDataConfig<*>>? = if(configs != null) SmartList() else null
		configs?.forEach { config ->
			val childConfigList = config.deepMergeConfigs(configContext)
			if(childConfigList.isNotEmpty()) {
				for(childConfig in childConfigList) {
					mergedConfigs?.add(childConfig)
				}
			}
		}
		when(this) {
			is CwtValueConfig -> {
				val mergedConfig = copy(configs = mergedConfigs).also { it.parent = parent }
				if(configContext.injectors.isNotEmpty()) return SmartList(mergedConfig)
				return mergedConfig.toSingletonList()
			}
			is CwtPropertyConfig -> {
				val subtypeExpression = key.removeSurroundingOrNull("subtype[", "]")
				if(subtypeExpression == null) {
					val mergedConfig = copy(configs = mergedConfigs).also { it.parent = parent }
					if(configContext.injectors.isNotEmpty()) return SmartList(mergedConfig)
					return mergedConfig.toSingletonList()
				} else {
					val subtypes = configContext.definitionSubtypes
					if(subtypes == null || ParadoxDefinitionSubtypeExpression.resolve(subtypeExpression).matches(subtypes)){
						if(configContext.injectors.isNotEmpty()) return mergedConfigs ?: SmartList()
						return mergedConfigs.orEmpty()
					} else {
						if(configContext.injectors.isNotEmpty()) return SmartList()
						return emptyList()
					}
				}
			}
		}
	}
	
	object Keys
}

val CwtDataConfig<*>.isRoot get() = when {
	this is CwtPropertyConfig -> this.parent == null
	this is CwtValueConfig -> this.parent == null && this.propertyConfig == null
	else -> false
}

val CwtDataConfig<*>.memberConfig get() = when {
	this is CwtPropertyConfig -> this
	this is CwtValueConfig -> propertyConfig ?: this
	else -> this
}

fun CwtDataConfig<*>.findOption(key: String): CwtOptionConfig? = options?.find { it.key == key }

fun CwtDataConfig<*>.findOptions(key: String): List<CwtOptionConfig> = options?.filter { it.key == key }.orEmpty()

val CwtDataConfig.Keys.cardinality by lazy { Key.create<CwtCardinalityExpression?>("paradox.cwtDataConfig.cardinality") }
val CwtDataConfig.Keys.cardinalityMinDefine by lazy { Key.create<String?>("paradox.cwtDataConfig.cardinalityMinDefine") }
val CwtDataConfig.Keys.cardinalityMaxDefine by lazy { Key.create<String?>("paradox.cwtDataConfig.cardinalityMaxDefine") }
val CwtDataConfig.Keys.hasScopeOption by lazy { Key.create<Boolean>("paradox.cwtDataConfig.hasScopeOption") }
val CwtDataConfig.Keys.scopeContext by lazy { Key.create<ParadoxScopeContext?>("paradox.cwtDataConfig.scopeContext") }
val CwtDataConfig.Keys.replaceScopes by lazy { Key.create<Map<String, String?>>("paradox.cwtDataConfig.replaceScopes") }
val CwtDataConfig.Keys.pushScope by lazy { Key.create<String?>("paradox.cwtDataConfig.pushScope") }
val CwtDataConfig.Keys.supportedScopes by lazy { Key.create<Set<String>>("paradox.cwtDataConfig.supportedScopes") }

//may on:
// * a config expression in declaration config
// * a config expression in subtype structure config
val CwtDataConfig<*>.cardinality get() = getOrPutUserData(CwtDataConfig.Keys.cardinality) action@{
	val option = options?.find { it.key == "cardinality" }
	if(option == null) {
		//如果没有注明且类型是常量，则推断为 1..1
		if(expression.type == Constant) {
			return@action CwtCardinalityExpression.resolve("1..1")
		}
	}
	option?.stringValue?.let { s -> CwtCardinalityExpression.resolve(s) }
}
val CwtDataConfig<*>.cardinalityMinDefine get() = getOrPutUserData(CwtDataConfig.Keys.cardinalityMinDefine) action@{
	val option = options?.find { it.key == "cardinality_min_define" }
	option?.stringValue
}
val CwtDataConfig<*>.cardinalityMaxDefine get() = getOrPutUserData(CwtDataConfig.Keys.cardinalityMaxDefine) action@{
	val option = options?.find { it.key == "cardinality_max_define" }
	option?.stringValue
}

val CwtDataConfig<*>.hasScopeOption get() = getOrPutUserData(CwtDataConfig.Keys.hasScopeOption) action@{
	options?.any { it.key == "replace_scope" || it.key == "replace_scopes" || it.key == "push_scope" || it.key == "scope" || it.key == "scopes" }
		?: false
}
val CwtDataConfig<*>.scopeContext get() = getOrPutUserData(CwtDataConfig.Keys.scopeContext) action@{
	val map = replaceScopes ?: return@action null
	ParadoxScopeContext.resolve(map)
}
//may on:
// * a config expression in declaration config (include root expression, e.g. "army = { ... }")
// * a type config (e.g. "type[xxx] = { ... }")
// * a subtype config (e.g. "subtype[xxx] = { ... }")
val CwtDataConfig<*>.replaceScopes get() = getOrPutUserData(CwtDataConfig.Keys.replaceScopes) action@{
	val option = options?.find { it.key == "replace_scope" || it.key == "replace_scopes" }
	if(option == null) return@action null
	val options = option.options ?: return@action null
	options.associateBy({ it.key.lowercase() }, { it.stringValue?.let { v -> ParadoxScopeHandler.getScopeId(v) } })
}
//may on:
// * a config expression in declaration config (include root expression, e.g. "army = { ... }")
// * a type config (e.g. "type[xxx] = { ... }")
// * a subtype config (e.g. "subtype[xxx] = { ... }")
val CwtDataConfig<*>.pushScope get() = getOrPutUserData(CwtDataConfig.Keys.pushScope) action@{
	val option = options?.find { it.key == "push_scope" }
	option?.stringValue?.let { v -> ParadoxScopeHandler.getScopeId(v) }
}
//may on:
// * a config expression in declaration config
val CwtDataConfig<*>.supportedScopes get() = getOrPutUserData(CwtDataConfig.Keys.supportedScopes) action@{
	val option = options?.find { it.key == "scope" || it.key == "scopes" }
	buildSet {
		option?.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) }
		option?.optionValues?.forEach { it.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) } }
	}.ifEmpty { ParadoxScopeHandler.anyScopeIdSet }
}

fun <T : PsiElement> CwtDataConfig<T>.toOccurrence(contextElement: PsiElement, project: Project): Occurrence {
	val cardinality = this.cardinality ?: return Occurrence(0, null, null, false)
	val cardinalityMinDefine = this.cardinalityMinDefine
	val cardinalityMaxDefine = this.cardinalityMaxDefine
	val occurrence = Occurrence(0, cardinality.min, cardinality.max, cardinality.relaxMin)
	if(cardinalityMinDefine != null) {
		val defineValue = ParadoxDefineHandler.getDefineValue(contextElement, project, cardinalityMinDefine, Int::class.java)
		if(defineValue != null) {
			occurrence.min = defineValue
			occurrence.minDefine = cardinalityMinDefine
		}
	}
	if(cardinalityMaxDefine != null) {
		val defineValue = ParadoxDefineHandler.getDefineValue(contextElement, project, cardinalityMaxDefine, Int::class.java)
		if(defineValue != null) {
			occurrence.max = defineValue
			occurrence.maxDefine = cardinalityMaxDefine
		}
	}
	return occurrence
}

val CwtDataConfig.Keys.overriddenProvider by lazy { Key.create<ParadoxOverriddenConfigProvider>("paradox.cwtDataConfig.overriddenProvider") }

var CwtDataConfig<*>.overriddenProvider by CwtDataConfig.Keys.overriddenProvider
