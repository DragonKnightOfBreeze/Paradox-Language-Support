package icu.windea.pls.script.psi.stubs

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import icu.windea.pls.lang.psi.stubs.ParadoxStub
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.SCRIPTED_VARIABLE
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

@Suppress("UnstableApiUsage")
interface ParadoxScriptScriptedVariableStub : ParadoxStub<ParadoxScriptScriptedVariable> {
    val name: String

    abstract class Base(
        parent: StubElement<*>?
    ) : StubBase<ParadoxScriptScriptedVariable>(parent, SCRIPTED_VARIABLE), ParadoxScriptScriptedVariableStub {
        override fun toString(): String {
            return "ParadoxScriptScriptedVariableStub(name=$name, gameType=$gameType)"
        }
    }

    class Impl(
        parent: StubElement<*>?,
        override val name: String,
        override val gameType: ParadoxGameType,
    ) : Base(parent)

    class Dummy(
        parent: StubElement<*>?,
    ) : Base(parent) {
        override val name: String get() = ""
        override val gameType: ParadoxGameType get() = ParadoxGameType.Core

        override fun toString(): String {
            return "ParadoxScriptScriptedVariableStub.Dummy"
        }
    }
}


