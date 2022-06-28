package icu.windea.pls.core.library

import com.intellij.openapi.roots.libraries.*
import icu.windea.pls.model.*

class ParadoxLibraryProperties(
	var rootInfo: ParadoxRootInfo? = null
): LibraryProperties<ParadoxLibraryProperties>(){
	override fun getState(): ParadoxLibraryProperties {
		return this
	}
	
	override fun loadState(state: ParadoxLibraryProperties) {
		rootInfo = state.rootInfo
	}
	
	override fun equals(other: Any?): Boolean {
		return other is ParadoxLibraryProperties && rootInfo == other.rootInfo
	}
	
	override fun hashCode(): Int {
		return rootInfo.hashCode()
	}
}