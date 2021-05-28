package icu.windea.pls.core.library

import com.intellij.openapi.roots.libraries.*
import icu.windea.pls.model.*

abstract class ParadoxLibraryKind(gameType: ParadoxGameType) : PersistentLibraryKind<ParadoxLibraryProperties>("paradox.${gameType.key}") {
	object Ck2LibraryKind: ParadoxLibraryKind(ParadoxGameType.Ck2)
	object Ck3LibraryKind: ParadoxLibraryKind(ParadoxGameType.Ck3)
	object Eu4LibraryKind: ParadoxLibraryKind(ParadoxGameType.Eu4)
	object Hoi4LibraryKind: ParadoxLibraryKind(ParadoxGameType.Hoi4)
	object IrLibraryKind: ParadoxLibraryKind(ParadoxGameType.Ir)
	object StellarisLibraryKind: ParadoxLibraryKind(ParadoxGameType.Stellaris)
	object Vic2LibraryKind: ParadoxLibraryKind(ParadoxGameType.Vic2)
	
	override fun createDefaultProperties() = ParadoxLibraryProperties()
}