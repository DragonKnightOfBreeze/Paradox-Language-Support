package icu.windea.pls.core.library

import com.intellij.openapi.roots.libraries.*

//TODO 不知道是否应该保留，反正就这样吧

class ParadoxLibraryProperties: LibraryProperties<ParadoxLibraryProperties>(){
	companion object{
		val instance = ParadoxLibraryProperties()
	}
	
	override fun getState(): ParadoxLibraryProperties {
		return this
	}
	
	override fun loadState(state: ParadoxLibraryProperties) {
	
	}
	
	override fun equals(other: Any?): Boolean {
		return true
	}
	
	override fun hashCode(): Int {
		return 1
	}
}