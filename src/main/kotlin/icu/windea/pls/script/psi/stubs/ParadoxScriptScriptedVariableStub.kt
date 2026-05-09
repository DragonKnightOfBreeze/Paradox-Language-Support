package icu.windea.pls.script.psi.stubs

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import icu.windea.pls.lang.psi.stubs.ParadoxStub
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 封装变量的存根（本地/全局）。
 *
 * @property name 封装变量的名字。
 */
@Suppress("UnstableApiUsage")
sealed interface ParadoxScriptScriptedVariableStub : ParadoxStub<ParadoxScriptScriptedVariable> {
    val name: String

    private sealed class Base(
        parent: StubElement<*>?
    ) : StubBase<ParadoxScriptScriptedVariable>(parent, SCRIPTED_VARIABLE), ParadoxScriptScriptedVariableStub {
        override val gameType get() = parentStub?.gameType ?: ParadoxGameType.Core

        override fun getParentStub() = super.getParentStub() as? ParadoxStub<*>

        override fun toString(): String {
            return "ParadoxScriptScriptedVariableStub(name=$name, gameType=$gameType)"
        }
    }

    private class Impl(parent: StubElement<*>?, override val name: String) : Base(parent)

    private class Dummy(parent: StubElement<*>?) : Base(parent) {
        override val name get() = ""

        override fun toString(): String {
            return "ParadoxScriptScriptedVariableStub.Dummy(gameType=$gameType)"
        }
    }

    companion object {
        fun create(parent: StubElement<*>?, name: String): ParadoxScriptScriptedVariableStub {
            return Impl(parent, name)
        }

        fun createDummy(parent: StubElement<*>?): ParadoxScriptScriptedVariableStub {
            return Dummy(parent)
        }
    }
}
