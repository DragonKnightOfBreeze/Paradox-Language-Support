package icu.windea.pls.localisation.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.model.*

interface ParadoxLocalisationPropertyStub : StubElement<ParadoxLocalisationProperty> {
    val name: String
    val category: ParadoxLocalisationCategory
    val gameType: ParadoxGameType

    abstract class Base(
        parent: StubElement<*>
    ) : StubBase<ParadoxLocalisationProperty>(parent, ParadoxLocalisationPropertyStubElementType.INSTANCE), ParadoxLocalisationPropertyStub {
        override fun toString(): String {
            return "ParadoxLocalisationPropertyStub(name=$name, category=$category, gameType=$gameType)"
        }
    }

    //12 + 20 + 4 * 4 = 48 -> 48
    class Impl(
        parent: StubElement<*>,
        override val name: String,
        override val category: ParadoxLocalisationCategory,
        override val gameType: ParadoxGameType,
    ) : Base(parent)

    //12 + 20 = 32 -> 32
    class Dummy(
        parent: StubElement<*>
    ) : Base(parent) {
        override val name: String get() = ""
        override val category: ParadoxLocalisationCategory get() = ParadoxLocalisationCategory.Normal
        override val gameType: ParadoxGameType get() = ParadoxGameType.placeholder()

        override fun toString(): String {
            return "ParadoxLocalisationPropertyStub.Dummy"
        }
    }
}
