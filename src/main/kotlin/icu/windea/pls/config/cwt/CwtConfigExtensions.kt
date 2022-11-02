@file:Suppress("unused")

package icu.windea.pls.config.cwt

import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.model.*

internal typealias CwtConfigMap = MutableMap<String, CwtFileConfig>
internal typealias CwtConfigMaps = MutableMap<String, CwtConfigMap>

val MockCwtConfigGroup by lazy { CwtConfigGroup(ParadoxGameType.Stellaris, getDefaultProject(), emptyMap()) }

inline fun CwtDataConfig<*>.processParent(processor: ProcessEntry.(CwtDataConfig<*>) -> Boolean): Boolean {
	var parent = this.parent
	while(parent != null) {
		val result = ProcessEntry.processor(parent)
		if(!result) return false
		parent = parent.parent
	}
	return true
}

inline fun CwtDataConfig<*>.processParentProperty(processor: ProcessEntry.(CwtPropertyConfig) -> Boolean): Boolean {
	var parent = this.parent
	while(parent != null) {
		if(parent is CwtPropertyConfig) {
			val result = ProcessEntry.processor(parent)
			if(!result) return false
		}
		parent = parent.parent
	}
	return true
}

fun CwtDataConfig<*>.processDescendants(processor: ProcessEntry.(CwtDataConfig<*>) -> Boolean): Boolean {
	return doProcessDescendants(processor)
}

private fun CwtDataConfig<*>.doProcessDescendants(processor: ProcessEntry.(CwtDataConfig<*>) -> Boolean): Boolean {
	ProcessEntry.processor(this).also { if(!it) return false }
	this.properties?.process { it.doProcessDescendants(processor) }?.also { if(!it) return false }
	this.values?.process { it.doProcessDescendants(processor) }?.also { if(!it) return false }
	return true
}

inline fun <T> Iterable<T>.sortedByPriority(configGroup: CwtConfigGroup, crossinline expressionExtractor: (T) -> CwtDataExpression): List<T> {
	return this.sortedByDescending { CwtConfigHandler.getPriority(expressionExtractor(it), configGroup) }
}