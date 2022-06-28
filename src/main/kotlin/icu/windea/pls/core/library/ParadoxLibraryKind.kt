package icu.windea.pls.core.library

import com.intellij.openapi.roots.libraries.*

object ParadoxLibraryKind: PersistentLibraryKind<ParadoxLibraryProperties>("paradox") {
	override fun createDefaultProperties() = ParadoxLibraryProperties()
}