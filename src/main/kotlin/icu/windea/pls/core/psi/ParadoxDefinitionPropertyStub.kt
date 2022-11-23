package icu.windea.pls.core.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.core.model.*

interface ParadoxDefinitionPropertyStub<T : ParadoxDefinitionProperty> : StubElement<T> {
	val name: String?
	val type: String?
	val subtypes: List<String>?
	val rootKey: String?
	val gameType: ParadoxGameType?
}
