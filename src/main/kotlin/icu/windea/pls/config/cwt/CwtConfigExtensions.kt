@file:Suppress("unused")

package icu.windea.pls.config.cwt

import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.model.*

internal typealias CwtConfigMap = MutableMap<String, CwtFileConfig>
internal typealias CwtConfigMaps = MutableMap<String, CwtConfigMap>

val MockCwtConfigGroup by lazy { CwtConfigGroup(ParadoxGameType.Stellaris, getDefaultProject(), emptyMap()) }

inline fun CwtKvConfig<*>.processParent(processor: (CwtKvConfig<*>) -> Boolean): Boolean {
	var parent = this.parent
	while(parent != null) {
		val result = processor(parent)
		if(!result) return false
		parent = parent.parent
	}
	return true
}

inline fun CwtKvConfig<*>.processParentProperty(processor: (CwtPropertyConfig) -> Boolean): Boolean {
	var parent = this.parent
	while(parent != null) {
		if(parent is CwtPropertyConfig) {
			val result = processor(parent)
			if(!result) return false
		}
		parent = parent.parent
	}
	return true
}