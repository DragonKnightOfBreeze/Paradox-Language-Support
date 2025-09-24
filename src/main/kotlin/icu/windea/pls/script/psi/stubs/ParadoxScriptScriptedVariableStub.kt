package icu.windea.pls.script.psi.stubs

import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import icu.windea.pls.lang.psi.stubs.ParadoxStub
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.SCRIPTED_VARIABLE
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

@Suppress("UnstableApiUsage")
/**
 * 脚本内（或全局）脚本变量的存根。
 *
 * - `name` 为变量名（不含 `@` 前缀）。
 * - `gameType` 从父存根（文件存根）继承。
 */
interface ParadoxScriptScriptedVariableStub : ParadoxStub<ParadoxScriptScriptedVariable> {
    val name: String

    abstract class Base(
        parent: StubElement<*>?
    ) : StubBase<ParadoxScriptScriptedVariable>(parent, SCRIPTED_VARIABLE), ParadoxScriptScriptedVariableStub {
        override val gameType: ParadoxGameType get() = parentStub?.gameType ?: ParadoxGameType.Core

        override fun getParentStub(): ParadoxStub<*>? {
            return super.getParentStub() as? ParadoxStub<*>
        }

        override fun toString(): String {
            return "ParadoxScriptScriptedVariableStub(name=$name, gameType=$gameType)"
        }
    }

    /** 具体实现。 */
    class Impl(
        parent: StubElement<*>?,
        override val name: String,
    ) : Base(parent)

    /** 空实现（占位）。 */
    class Dummy(
        parent: StubElement<*>?
    ) : Base(parent) {
        override val name: String get() = ""

        override fun toString(): String {
            return "ParadoxScriptScriptedVariableStub.Dummy(gameType=$gameType)"
        }
    }
}


