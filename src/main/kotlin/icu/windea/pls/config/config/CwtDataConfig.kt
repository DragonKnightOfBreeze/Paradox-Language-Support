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
import icu.windea.pls.lang.model.*
import java.util.*

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
	
	override fun resolved(): CwtDataConfig<*> = this
	
	override fun resolvedOrNull(): CwtDataConfig<*>? = null
	
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
				val mergedConfig = copy(value = value, configs = mergedConfigs).also { it.parent = parent }
				if(configContext.injectors.isNotEmpty()) return SmartList(mergedConfig)
				return mergedConfig.toSingletonList()
			}
			is CwtPropertyConfig -> {
				val subtypeExpression = key.removeSurroundingOrNull("subtype[", "]")
				if(subtypeExpression == null) {
					val mergedConfig = copy(key = key, value = value, configs = mergedConfigs).also { it.parent = parent }
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
	
	fun toOccurrence(contextElement: PsiElement, project: Project): Occurrence {
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
}

fun CwtDataConfig<*>.findOption(key: String): CwtOptionConfig? = options?.find { it.key == key }

fun CwtDataConfig<*>.findOptions(key: String): List<CwtOptionConfig> = options?.filter { it.key == key }.orEmpty()

object CwtDataConfigKeys {
	val path = Key.create<String>("paradox.cwtDataConfig.path")
	val cardinality = Key.create<CwtCardinalityExpression?>("paradox.cwtDataConfig.cardinality")
	val cardinalityMinDefine = Key.create<String?>("paradox.cwtDataConfig.cardinalityMinDefine")
	val cardinalityMaxDefine = Key.create<String?>("paradox.cwtDataConfig.cardinalityMaxDefine")
	val hasScopeOption = Key.create<Boolean>("paradox.cwtDataConfig.hasScopeOption")
	val replaceScopes = Key.create<ParadoxScopeContext?>("paradox.cwtDataConfig.replaceScopes")
	val pushScope = Key.create<String?>("paradox.cwtDataConfig.pushScope")
	val supportedScopes = Key.create<Set<String>>("paradox.cwtDataConfig.supportedScopes")
}

val CwtDataConfig<*>.path get() = getOrPutUserData(CwtDataConfigKeys.path) action@{
	val list = LinkedList<String>()
	var current: CwtDataConfig<*> = this
	while(true) {
		list.addFirst(current.expression.expressionString)
		current = current.resolved().parent ?: break
	}
	list.joinToString("/")
}

//may on:
// * a config expression in declaration config
// * a config expression in subtype structure config
val CwtDataConfig<*>.cardinality get() = getOrPutUserData(CwtDataConfigKeys.cardinality) action@{
	val option = options?.find { it.key == "cardinality" }
	if(option == null) {
		//如果没有注明且类型是常量，则推断为 1..1
		if(expression.type == Constant) {
			return@action CwtCardinalityExpression.resolve("1..1")
		}
	}
	option?.stringValue?.let { s -> CwtCardinalityExpression.resolve(s) }
}
val CwtDataConfig<*>.cardinalityMinDefine get() = getOrPutUserData(CwtDataConfigKeys.cardinalityMinDefine) action@{
	val option = options?.find { it.key == "cardinality_min_define" }
	option?.stringValue
}
val CwtDataConfig<*>.cardinalityMaxDefine get() = getOrPutUserData(CwtDataConfigKeys.cardinalityMaxDefine) action@{
	val option = options?.find { it.key == "cardinality_max_define" }
	option?.stringValue
}

val CwtDataConfig<*>.hasScopeOption get() = getOrPutUserData(CwtDataConfigKeys.hasScopeOption) action@{
	options?.any { it.key == "replace_scope" || it.key == "replace_scopes" || it.key == "push_scope" || it.key == "scope" || it.key == "scopes" }
		?: false
}
//may on:
// * a config expression in declaration config (include root expression, e.g. "army = { ... }")
// * a type config (e.g. "type[xxx] = { ... }")
// * a subtype config (e.g. "subtype[xxx] = { ... }")
val CwtDataConfig<*>.replaceScopes get() = getOrPutUserData(CwtDataConfigKeys.replaceScopes) action@{
	val option = options?.find { it.key == "replace_scope" || it.key == "replace_scopes" }
	if(option == null) return@action null
	val options = option.options ?: return@action null
	val map = options.associateBy({ it.key.lowercase() }, { it.stringValue?.let { v -> ParadoxScopeHandler.getScopeId(v) } })
	ParadoxScopeContext.resolve(map)
}
//may on:
// * a config expression in declaration config (include root expression, e.g. "army = { ... }")
// * a type config (e.g. "type[xxx] = { ... }")
// * a subtype config (e.g. "subtype[xxx] = { ... }")
val CwtDataConfig<*>.pushScope get() = getOrPutUserData(CwtDataConfigKeys.pushScope) action@{
	val option = options?.find { it.key == "push_scope" }
	option?.stringValue?.let { v -> ParadoxScopeHandler.getScopeId(v) }
}
//may on:
// * a config expression in declaration config
val CwtDataConfig<*>.supportedScopes get() = getOrPutUserData(CwtDataConfigKeys.supportedScopes) action@{
	val option = options?.find { it.key == "scope" || it.key == "scopes" }
	buildSet {
		option?.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) }
		option?.optionValues?.forEach { it.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) } }
	}.ifEmpty { ParadoxScopeHandler.anyScopeIdSet }
}