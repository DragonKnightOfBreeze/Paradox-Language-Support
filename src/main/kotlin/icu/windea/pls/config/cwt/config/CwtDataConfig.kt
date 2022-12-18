package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
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
	
	var parent: CwtDataConfig<*>? = null
	
	val isBlock: Boolean get() = configs != null
	val values : List<CwtValueConfig>? by lazy { configs?.filterIsInstance<CwtValueConfig>() }
	val properties: List<CwtPropertyConfig>? by lazy { configs?.filterIsInstance<CwtPropertyConfig>() }
	
	val cardinality by lazy { resolveCardinality() }
	val scope get() = resolveScope() //不要缓存，因为parent可能有变动
	val scopeMap get() = resolveScopeMap() //不要缓存，因为parent可能有变动
	
	private fun resolveCardinality(): CwtCardinalityExpression? {
		return options?.find { it.key == "cardinality" }?.stringValue?.let { s -> CwtCardinalityExpression.resolve(s) }
	}
	
	//TODO check
	
	private fun resolveScope(): String? {
		//option的名字可能是：replace_scope/replace_scopes/push_scope
		//对应的option可能位于：alias规则定义上，上一级definitionElement规则定义上，definition规则定义上，subtype规则定义上
		var current: CwtDataConfig<*>? = this
		while(current != null) {
			val scope = doResolveScope(current)
			if(scope != null) return scope
			current = current.parent
		}
		return null
	}
	
	private fun doResolveScope(config: CwtDataConfig<*>): String? {
		val options = config.options ?: return null
		return options.find { it.key == "push_scope" }?.value
			?: options.find { it.key == "replace_scope" || it.key == "replace_scopes" }?.options
				?.find { o -> o.key == "this" }?.value
	}
	
	
	private fun resolveScopeMap(): MutableMap<String, String> {
		//option的名字可能是：replace_scope/replace_scopes/push_scope
		//对应的option可能位于：alias规则定义上，上一级definitionElement规则定义上，definition规则定义上，subtype规则定义上
		val result: MutableMap<String, String> = mutableMapOf()
		var current: CwtDataConfig<*>? = this
		while(current != null) {
			doResolveScopeMap(current, result)
			current = current.parent
		}
		return result
	}
	
	private fun doResolveScopeMap(config: CwtDataConfig<*>, scopeMap: MutableMap<String, String>) {
		val options = config.options ?: return
		options.find { it.key == "push_scope" }?.value?.let { scopeMap.putIfAbsent("this", it) }
		options.find { it.key == "replace_scope" || it.key == "replace_scopes" }?.options?.let {
			for(option in it) scopeMap.putIfAbsent(option.key, option.value)
		}
	}
	
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
	 * 深拷贝 + 根据子类型进行合并。
	 */
	fun deepMergeBySubtypes(subtypes: List<String>): List<CwtDataConfig<*>> {
		val mergedConfigs: MutableList<CwtDataConfig<*>>? = if(configs != null) SmartList() else null
		configs?.forEach { config ->
			val childConfigList = config.deepMergeBySubtypes(subtypes)
			if(childConfigList.isEmpty()) return@forEach
			for(childConfig in childConfigList) {
				mergedConfigs?.add(childConfig)
			}
		}
		return when(this) {
			is CwtValueConfig -> {
				copy(configs = mergedConfigs).also { parent = it.parent }.toSingletonList()
			}
			is CwtPropertyConfig -> {
				val subtypeName = key.removeSurroundingOrNull("subtype[", "]")
					?: return copy(configs = mergedConfigs).also { parent = it.parent }.toSingletonList()
				if(!matchesDefinitionSubtypeExpression(subtypeName, subtypes)) return emptyList()
				mergedConfigs.orEmpty()
			}
		}
	}
}
