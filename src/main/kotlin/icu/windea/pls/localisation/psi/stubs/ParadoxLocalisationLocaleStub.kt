package icu.windea.pls.localisation.psi.stubs

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import icu.windea.pls.lang.psi.stubs.ParadoxLocaleAwareStub
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.model.ParadoxGameType

/**
 * 本地化语言区域的存根。
 */
@Suppress("UnstableApiUsage")
sealed interface ParadoxLocalisationLocaleStub : ParadoxLocaleAwareStub<ParadoxLocalisationLocale> {
    override fun getParentStub(): ParadoxLocalisationPropertyListStub?

    private sealed class Base(
        parent: StubElement<*>?
    ) : StubBase<ParadoxLocalisationLocale>(parent, LOCALE), ParadoxLocalisationLocaleStub {
        override val locale get() = parentStub?.locale
        override val gameType get() = parentStub?.gameType ?: ParadoxGameType.Core

        override fun getParentStub() = super.getParentStub() as? ParadoxLocalisationPropertyListStub

        override fun toString(): String {
            return "ParadoxLocalisationPropertyListStub(locale=$locale, gameType=$gameType)"
        }
    }

    private class Impl(parent: StubElement<*>?) : Base(parent)

    companion object {
        fun create(parent: StubElement<*>?): ParadoxLocalisationLocaleStub {
            return Impl(parent)
        }
    }
}
