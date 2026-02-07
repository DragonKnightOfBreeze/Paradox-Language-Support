package icu.windea.pls.localisation.psi.stubs

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import icu.windea.pls.lang.psi.stubs.ParadoxLocaleAwareStub
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxLocalisationType

/**
 * 本地化（属性）列表的存根。
 *
 * @property localisationType 本地化的类型。来自父存根（[ParadoxLocalisationFileStub]）。
 */
@Suppress("UnstableApiUsage")
sealed interface ParadoxLocalisationPropertyListStub : ParadoxLocaleAwareStub<ParadoxLocalisationPropertyList> {
    val localisationType: ParadoxLocalisationType

    override fun getParentStub(): ParadoxLocalisationFileStub?

    private sealed class Base(
        parent: StubElement<*>?
    ) : StubBase<ParadoxLocalisationPropertyList>(parent, PROPERTY_LIST), ParadoxLocalisationPropertyListStub {
        override val localisationType get() = parentStub?.localisationType ?: ParadoxLocalisationType.Normal
        override val gameType get() = parentStub?.gameType ?: ParadoxGameType.Core

        override fun getParentStub() = super.getParentStub() as? ParadoxLocalisationFileStub

        override fun toString(): String {
            return "ParadoxLocalisationPropertyListStub(locale=$locale, localisationType=$localisationType, gameType=$gameType)"
        }
    }

    private class Impl(parent: StubElement<*>?, override val locale: String?) : Base(parent)

    companion object {
        fun create(parent: StubElement<*>?, locale: String?): ParadoxLocalisationPropertyListStub {
            return Impl(parent, locale)
        }
    }
}
