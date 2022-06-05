package icu.windea.pls.config.cwt.config

import com.intellij.psi.*
import icu.windea.pls.config.cwt.expression.*

abstract class CwtKvConfig<out T : PsiElement> : CwtConfig<T> {
	abstract val properties: List<CwtPropertyConfig>?
	abstract val values: List<CwtValueConfig>?
	abstract val documentation: String?
	abstract val options: List<CwtOptionConfig>?
	abstract val optionValues: List<CwtOptionValueConfig>?
	
	var parent: CwtPropertyConfig? = null
	var aliasName: String? = null //用于规则内联
	
	val cardinality by lazy { inferCardinality() }
	val scope get() = inferScope() //不要缓存，因为parent可能有变动
	val scopeMap get() = inferScopeMap() //不要缓存，因为parent可能有变动
	
	private fun inferCardinality(): CwtCardinalityExpression? {
		return options?.find { it.key == "cardinality" }?.stringValue?.let { s -> CwtCardinalityExpression.resolve(s) }
	}
	
	private fun inferScope(): String? {
		//option的名字可能是：replace_scope/replace_scopes/push_scope
		//对应的option可能位于：alias规则定义上，上一级definitionProperty规则定义上，definition规则定义上，subtype规则定义上
		var current: CwtKvConfig<*>? = this
		while(current != null) {
			val scope = doInferScope(current)
			if(scope != null) return scope
			current = current.parent
		}
		return null
	}
	
	private fun doInferScope(config: CwtKvConfig<*>): String? {
		val options = config.options ?: return null
		return options.find { it.key == "push_scope" }?.value
			?: options.find { it.key == "replace_scope" || it.key == "replace_scopes" }?.options
				?.find { o -> o.key == "this" }?.value
	}
	
	
	private fun inferScopeMap(): MutableMap<String, String> {
		//option的名字可能是：replace_scope/replace_scopes/push_scope
		//对应的option可能位于：alias规则定义上，上一级definitionProperty规则定义上，definition规则定义上，subtype规则定义上
		val result: MutableMap<String, String> = mutableMapOf()
		var current: CwtKvConfig<*>? = this
		while(current != null) {
			doInferScopeMap(current, result)
			current = current.parent
		}
		return result
	}
	
	private fun doInferScopeMap(config: CwtKvConfig<*>, scopeMap: MutableMap<String, String>) {
		val options = config.options ?: return
		options.find { it.key == "push_scope" }?.value?.let { scopeMap.putIfAbsent("this", it) }
		options.find { it.key == "replace_scope" || it.key == "replace_scopes" }?.options?.let {
			for(option in it) scopeMap.putIfAbsent(option.key, option.value)
		}
	}
}