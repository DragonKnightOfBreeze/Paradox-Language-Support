package icu.windea.pls.localisation.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.core.model.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.core.model.*

class ParadoxLocalisationStubImpl(
	parent: StubElement<*>,
	override val name: String? = null,
	override val category: ParadoxLocalisationCategory,
	override val gameType: ParadoxGameType
) : StubBase<ParadoxLocalisationProperty>(parent, ParadoxLocalisationStubElementTypes.PROPERTY), ParadoxLocalisationStub{
	override fun toString(): String {
		return "ParadoxLocalisationStub(name=$name, category=$category, gameType=$gameType)"
	}
}