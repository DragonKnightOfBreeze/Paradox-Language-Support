package icu.windea.pls.localisation.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationStubImpl(
	parent: StubElement<*>,
	override val name: String?,
	override val category: ParadoxLocalisationCategory,
	override val locale: String?,
	override val gameType: ParadoxGameType?
) : StubBase<ParadoxLocalisationProperty>(parent, ParadoxLocalisationStubElementTypes.PROPERTY), ParadoxLocalisationStub{
	override fun toString(): String {
		return "ParadoxLocalisationStub(name=$name, category=$category, locale=$locale, gameType=$gameType)"
	}
}