package com.windea.plugin.idea.pls.core.library

import com.intellij.openapi.roots.libraries.*

abstract class ParadoxLibraryKind(kindId:String) : PersistentLibraryKind<ParadoxLibraryProperties>(kindId) {
	override fun createDefaultProperties(): ParadoxLibraryProperties {
		return ParadoxLibraryProperties()
	}
}