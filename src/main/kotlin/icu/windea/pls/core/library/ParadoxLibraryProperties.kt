package icu.windea.pls.core.library

import com.intellij.openapi.roots.libraries.*

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
		return other is ParadoxLibraryProperties
	}
	
	override fun hashCode(): Int {
		return 0
	}
}