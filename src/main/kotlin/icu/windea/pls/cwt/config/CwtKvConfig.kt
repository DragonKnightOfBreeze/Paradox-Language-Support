package icu.windea.pls.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.expression.*

abstract class CwtKvConfig<out T : PsiElement> : CwtConfig<T> {
	abstract val documentation: String?
	abstract val options: List<CwtOptionConfig>?
	abstract val optionValues: List<CwtOptionValueConfig>?
	abstract var parent: CwtPropertyConfig?
	
	//懒加载
	val cardinality by lazy { inferCardinality() }
	val scope by lazy { inferScope() }
	val scopeMap by lazy { inferScopeMap() }
	
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
	
	
	private fun inferScopeMap(): MutableMap<String,String> {
		//option的名字可能是：replace_scope/replace_scopes/push_scope
		//对应的option可能位于：alias规则定义上，上一级definitionProperty规则定义上，definition规则定义上，subtype规则定义上
		val result:MutableMap<String,String> = mutableMapOf()
		var current: CwtKvConfig<*>? = this
		while(current != null) {
			doInferScopeMap(current,result)
			current = current.parent
		}
		return result
	}
	
	private fun doInferScopeMap(config: CwtKvConfig<*>, scopeMap: MutableMap<String, String>) {
		val options = config.options ?: return
		options.find { it.key == "push_scope" }?.value?.let { scopeMap.putIfAbsent("this", it) }
		options.find { it.key == "replace_scope" || it.key == "replace_scopes" }?.options?.let {
			for(option in options) scopeMap.putIfAbsent(option.key, option.value)
		}
	}
}