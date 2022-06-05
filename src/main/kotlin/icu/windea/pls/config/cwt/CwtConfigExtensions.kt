@file:Suppress("unused")

package icu.windea.pls.config.cwt

import icu.windea.pls.config.cwt.config.*

internal typealias CwtConfigMap = MutableMap<String, CwtFileConfig>
internal typealias CwtConfigMaps = MutableMap<String, CwtConfigMap>

fun CwtPropertyConfig.processParent(processor: (CwtPropertyConfig) -> Boolean): Boolean{
	var parent = this.parent
	while(parent != null){
		val result = processor(parent)
		if(!result) return false
		parent = parent.parent
	}
	return true
}