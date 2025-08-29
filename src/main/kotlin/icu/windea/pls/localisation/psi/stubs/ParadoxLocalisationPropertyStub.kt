package icu.windea.pls.localisation.psi.stubs

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import icu.windea.pls.lang.psi.stubs.ParadoxLocaleAwareStub
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.PROPERTY
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxLocalisationType

@Suppress("UnstableApiUsage")
interface ParadoxLocalisationPropertyStub : ParadoxLocaleAwareStub<ParadoxLocalisationProperty> {
    val name: String
    val type: ParadoxLocalisationType

    override fun getParentStub(): ParadoxLocalisationPropertyListStub?

    abstract class Base(
        parent: StubElement<*>?
    ) : StubBase<ParadoxLocalisationProperty>(parent, PROPERTY), ParadoxLocalisationPropertyStub {
        override val locale: String? get() = parentStub?.locale
        override val type: ParadoxLocalisationType get() = parentStub?.localisationType ?: ParadoxLocalisationType.Normal
        override val gameType: ParadoxGameType get() = parentStub?.gameType ?: ParadoxGameType.placeholder()

        override fun getParentStub(): ParadoxLocalisationPropertyListStub? {
            return super.getParentStub() as? ParadoxLocalisationPropertyListStub
        }

        override fun toString(): String {
            return "ParadoxLocalisationPropertyStub(name=$name, type=$type, locale=$locale, gameType=$gameType)"
        }
    }

    class Impl(
        parent: StubElement<*>?,
        override val name: String,
    ) : Base(parent)

    class Dummy(
        parent: StubElement<*>?
    ) : Base(parent) {
        override val name: String get() = ""

        override fun toString(): String {
            return "ParadoxLocalisationPropertyStub.Dummy"
        }
    }
}
