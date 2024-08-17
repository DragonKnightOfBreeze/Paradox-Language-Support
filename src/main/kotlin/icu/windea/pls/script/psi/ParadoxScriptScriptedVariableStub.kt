package icu.windea.pls.script.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.model.*

interface ParadoxScriptScriptedVariableStub : StubElement<ParadoxScriptScriptedVariable> {
    val name: String
    val gameType: ParadoxGameType
    
    abstract class Base(
        parent: StubElement<*>
    ) : StubBase<ParadoxScriptScriptedVariable>(parent, ParadoxScriptStubElementTypes.SCRIPTED_VARIABLE), ParadoxScriptScriptedVariableStub {
        override fun toString(): String {
            return "ParadoxScriptScriptedVariableStub(name=$name, gameType=$gameType)"
        }
    }
    
    //12 + 20 + 4 * 2 = 40 -> 40
    class Impl(
        parent: StubElement<*>,
        override val name: String,
        override val gameType: ParadoxGameType,
    ) : Base(parent)
    
    //12 + 20 = 32 -> 32
    class Dummy(
        parent: StubElement<*>,
    ) : Base(parent) {
        override val name: String get() = ""
        override val gameType: ParadoxGameType get() = ParadoxGameType.placeholder()
        
        override fun toString(): String {
            return "ParadoxScriptScriptedVariableStub.Dummy"
        }
    }
}


