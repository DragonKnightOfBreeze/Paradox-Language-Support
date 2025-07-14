package icu.windea.pls.csv.psi

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import icu.windea.pls.model.ParadoxGameType

interface ParadoxCsvRowStub : StubElement<ParadoxCsvRow> {
    val name: String
    val gameType: ParadoxGameType

    //TODO 2.0.1.dev

    abstract class Base(
        parent: StubElement<*>
    ) : StubBase<ParadoxCsvRow>(parent, ParadoxCsvRowStubElementType.INSTANCE), ParadoxCsvRowStub {
        override fun toString(): String {
            return "ParadoxCsvRowStub(name=$name, gameType=$gameType)"
        }
    }

    //12 + 20 + 2 * 4 = 36 -> 40
    class Impl(
        parent: StubElement<*>,
        override val name: String,
        override val gameType: ParadoxGameType
    ) : Base(parent)

    //12 + 20 = 32 -> 32
    class Dummy(
        parent: StubElement<*>,
    ) : Base(parent) {
        override val name: String get() = ""
        override val gameType: ParadoxGameType get() = ParadoxGameType.Companion.placeholder()

        override fun toString(): String {
            return "ParadoxCsvRowStub.Dummy"
        }
    }
}
