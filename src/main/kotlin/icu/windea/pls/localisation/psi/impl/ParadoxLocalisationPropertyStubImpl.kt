package icu.windea.pls.localisation.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*

class ParadoxLocalisationPropertyStubImpl(
    parent: StubElement<*>,
    override val name: String,
    override val category: ParadoxLocalisationCategory,
    override val locale: String?,
    override val gameType: ParadoxGameType?
) : StubBase<ParadoxLocalisationProperty>(parent, ParadoxLocalisationStubElementTypes.PROPERTY), ParadoxLocalisationPropertyStub {
    override fun toString(): String {
        return "ParadoxLocalisationPropertyStub(name=$name, category=$category, locale=$locale, gameType=$gameType)"
    }
}