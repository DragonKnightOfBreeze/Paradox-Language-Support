package icu.windea.pls.localisation.psi.stubs

import com.intellij.psi.stubs.*
import icu.windea.pls.lang.psi.stubs.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.model.*

@Suppress("UnstableApiUsage")
interface ParadoxLocalisationPropertyListStub : ParadoxLocaleAwareStub<ParadoxLocalisationPropertyList> {
    val localisationType: ParadoxLocalisationType

    override fun getParentStub(): ParadoxLocalisationFileStub?

    abstract class Base(
        parent: StubElement<*>?
    ) : StubBase<ParadoxLocalisationPropertyList>(parent, PROPERTY_LIST), ParadoxLocalisationPropertyListStub {
        override val localisationType: ParadoxLocalisationType get() = parentStub?.localisationType ?: ParadoxLocalisationType.Normal
        override val gameType: ParadoxGameType get() = parentStub?.gameType ?: ParadoxGameType.placeholder()

        override fun getParentStub(): ParadoxLocalisationFileStub? {
            return super.getParentStub() as? ParadoxLocalisationFileStub
        }

        override fun toString(): String {
            return "ParadoxLocalisationPropertyListStub(locale=$locale, localisationType=$localisationType, gameType=$gameType)"
        }
    }

    class Impl(
        parent: StubElement<*>?,
        override val locale: String?
    ) : Base(parent)
}
