package icu.windea.pls.localisation.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.model.*

interface ParadoxLocalisationPropertyStub : StubElement<ParadoxLocalisationProperty> {
    val name: String
    val type: ParadoxLocalisationType
    val gameType: ParadoxGameType

    abstract class Base(
        parent: StubElement<*>
    ) : StubBase<ParadoxLocalisationProperty>(parent, ParadoxLocalisationPropertyStubElementType.INSTANCE), ParadoxLocalisationPropertyStub {
        override fun toString(): String {
            return "ParadoxLocalisationPropertyStub(name=$name, category=$type, gameType=$gameType)"
        }
    }

    //12 + 20 + 4 * 4 = 48 -> 48
    class Impl(
        parent: StubElement<*>,
        override val name: String,
        override val type: ParadoxLocalisationType,
        override val gameType: ParadoxGameType,
    ) : Base(parent)

    //12 + 20 = 32 -> 32
    class Dummy(
        parent: StubElement<*>
    ) : Base(parent) {
        override val name: String get() = ""
        override val type: ParadoxLocalisationType get() = ParadoxLocalisationType.Normal
        override val gameType: ParadoxGameType get() = ParadoxGameType.placeholder()

        override fun toString(): String {
            return "ParadoxLocalisationPropertyStub.Dummy"
        }
    }
}
