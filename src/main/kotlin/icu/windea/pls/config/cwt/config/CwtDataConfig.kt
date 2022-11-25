package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*

abstract class CwtDataConfig<out T : PsiElement> : CwtConfig<T> {
	abstract val value: String
	abstract val booleanValue: Boolean?
	abstract val intValue: Int?
	abstract val floatValue: Float?
	abstract val stringValue: String?
	abstract val properties: List<CwtPropertyConfig>?
	abstract val values: List<CwtValueConfig>?
	abstract val documentation: String?
	abstract val options: List<CwtOptionConfig>?
	abstract val optionValues: List<CwtOptionValueConfig>?
	
	abstract override val expression: CwtDataExpression
	
	var parent: CwtDataConfig<*>? = null
	
	val isBlock: Boolean get() = properties != null || values != null
	
	val cardinality by lazy { resolveCardinality() }
	val scope get() = resolveScope() //不要缓存，因为parent可能有变动
	val scopeMap get() = resolveScopeMap() //不要缓存，因为parent可能有变动
	
	private fun resolveCardinality(): CwtCardinalityExpression? {
		return options?.find { it.key == "cardinality" }?.stringValue?.let { s -> CwtCardinalityExpression.resolve(s) }
	}
	
	//TODO check
	
	private fun resolveScope(): String? {
		//option的名字可能是：replace_scope/replace_scopes/push_scope
		//对应的option可能位于：alias规则定义上，上一级definitionProperty规则定义上，definition规则定义上，subtype规则定义上
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
		//对应的option可能位于：alias规则定义上，上一级definitionProperty规则定义上，definition规则定义上，subtype规则定义上
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
	
	abstract fun resolved(): CwtDataConfig<*>
	
	abstract fun resolvedOrNull(): CwtDataConfig<*>?
	
	//深拷贝
	
	fun deepCopyProperties(): List<CwtPropertyConfig>? {
		return properties?.map { p -> p.copy(properties = p.deepCopyProperties(), values = p.deepCopyValues()).also { it.parent = this } }
	}
	
	fun deepCopyValues(): List<CwtValueConfig>? {
		return values?.map { v -> v.copy(properties = v.deepCopyProperties(), values = v.deepCopyValues()).also { it.parent = this } }
	}
	
	//深拷贝 + 根据子类型进行合并
	
	fun deepMergeBySubtypes(subtypes: List<String>): List<CwtDataConfig<*>> {
		val properties = properties
		val values = values
		var mergedProperties: MutableList<CwtPropertyConfig>? = if(properties != null) SmartList() else null
		var mergedValues: MutableList<CwtValueConfig>? = if(values != null) SmartList() else null
		if(properties != null && properties.isNotEmpty()) {
			for(propConfig in properties) {
				val configList = propConfig.deepMergeBySubtypes(subtypes)
				if(configList.isEmpty()) continue
				for(config in configList) {
					when {
						config is CwtPropertyConfig -> {
							mergedProperties?.add(config)
						}
						config is CwtValueConfig -> {
							if(mergedValues == null) mergedValues = SmartList()
							mergedValues.add(config)
						}
					}
				}
			}
		}
		if(values != null && values.isNotEmpty()) {
			for(valueConfig in values) {
				val configList = valueConfig.deepMergeBySubtypes(subtypes)
				if(configList.isEmpty()) continue
				for(config in configList) {
					when {
						config is CwtPropertyConfig -> {
							if(mergedProperties == null) mergedProperties = SmartList()
							mergedProperties.add(config)
						}
						config is CwtValueConfig -> {
							mergedValues?.add(config)
						}
					}
				}
			}
		}
		when(this) {
			is CwtPropertyConfig -> {
				val subtypeName = key.removeSurroundingOrNull("subtype[", "]")
					?: return copy(properties = mergedProperties, values = mergedValues).also { parent = it.parent }.toSingletonList()
				if(!matchesDefinitionSubtypeExpression(subtypeName, subtypes)) return emptyList()
				return mergedProperties.orEmpty() + mergedValues.orEmpty()
			}
			is CwtValueConfig -> {
				return copy(properties = mergedProperties, values = mergedValues).also { parent = it.parent }.toSingletonList()
			}
			else -> return emptyList()
		}
	}
	
	//fun deepMergeProperties(subtypes: List<String>): List<CwtPropertyConfig>? {
	//	return properties?.flatMap { p ->
	//		val subtypeName = p.key.removeSurroundingOrNull("subtype[", "]")
	//		if(subtypeName != null && matchesDefinitionSubtypeExpression(subtypeName, subtypes)) {
	//			val list = SmartList<CwtPropertyConfig>()
	//			list
	//		} else {
	//			p.copy(properties = p.deepMergeProperties(subtypes), values = p.deepMergeValues(subtypes)).also { it.parent = this }.toSingletonList()
	//		}
	//	}
	//}
	//
	//fun deepMergeValues(subtypes: List<String>): List<CwtValueConfig>? {
	//	return values?.map { v -> 
	//		v.copy(properties = v.deepMergeProperties(subtypes), values = v.deepMergeValues(subtypes)).also { it.parent = this }
	//	}
	//}
}
