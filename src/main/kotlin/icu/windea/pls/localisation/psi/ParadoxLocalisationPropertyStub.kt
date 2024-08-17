package icu.windea.pls.localisation.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.model.*

interface ParadoxLocalisationPropertyStub : StubElement<ParadoxLocalisationProperty> {
    val name: String
    val category: ParadoxLocalisationCategory
    val locale: String?
    val gameType: ParadoxGameType
    
    fun isValid() = name.isNotEmpty()
    
    class Impl(
        parent: StubElement<*>,
        override val name: String,
        override val category: ParadoxLocalisationCategory,
        override val locale: String?,
        override val gameType: ParadoxGameType
    ) : StubBase<ParadoxLocalisationProperty>(parent, ParadoxLocalisationStubElementTypes.PROPERTY), ParadoxLocalisationPropertyStub {
        override fun toString(): String {
            return "ParadoxLocalisationPropertyStub(name=$name, category=$category, locale=$locale, gameType=$gameType)"
        }
    }
}

class ParadoxLocalisationPropertyStubImpl(
    parent: StubElement<*>,
    override val name: String,
    override val category: ParadoxLocalisationCategory,
    override val locale: String?,
    override val gameType: ParadoxGameType
) : StubBase<ParadoxLocalisationProperty>(parent, ParadoxLocalisationStubElementTypes.PROPERTY), ParadoxLocalisationPropertyStub {
    override fun toString(): String {
        return "ParadoxLocalisationPropertyStub(name=$name, category=$category, locale=$locale, gameType=$gameType)"
    }
}
