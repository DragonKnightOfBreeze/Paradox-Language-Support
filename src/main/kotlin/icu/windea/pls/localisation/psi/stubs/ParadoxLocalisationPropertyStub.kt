package icu.windea.pls.localisation.psi.stubs

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import icu.windea.pls.lang.psi.stubs.ParadoxLocaleAwareStub
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxLocalisationType

/**
 * 本地化（属性）的存根。
 *
 * @property name 本地化的名字。
 * @property type 本地化的类型。来自父存根（[ParadoxLocalisationPropertyListStub]）。
 */
@Suppress("UnstableApiUsage")
sealed interface ParadoxLocalisationPropertyStub : ParadoxLocaleAwareStub<ParadoxLocalisationProperty> {
    val name: String
    val type: ParadoxLocalisationType

    override fun getParentStub(): ParadoxLocalisationPropertyListStub?

    private sealed class Base(
        parent: StubElement<*>?
    ) : StubBase<ParadoxLocalisationProperty>(parent, PROPERTY), ParadoxLocalisationPropertyStub {
        override val locale get() = parentStub?.locale
        override val type get() = parentStub?.localisationType ?: ParadoxLocalisationType.Normal
        override val gameType get() = parentStub?.gameType ?: ParadoxGameType.Core

        override fun getParentStub() = super.getParentStub() as? ParadoxLocalisationPropertyListStub

        override fun toString(): String {
            return "ParadoxLocalisationPropertyStub(name=$name, type=$type, locale=$locale, gameType=$gameType)"
        }
    }

    private class Impl(parent: StubElement<*>?, override val name: String) : Base(parent)

    private class Dummy(parent: StubElement<*>?) : Base(parent) {
        override val name: String get() = ""

        override fun toString(): String {
            return "ParadoxLocalisationPropertyStub.Dummy(gameType=$gameType)"
        }
    }

    companion object {
        fun create(parent: StubElement<*>?, name: String): ParadoxLocalisationPropertyStub {
            return Impl(parent, name)
        }

        fun createDummy(parent: StubElement<*>?): ParadoxLocalisationPropertyStub {
            return Dummy(parent)
        }
    }
}
