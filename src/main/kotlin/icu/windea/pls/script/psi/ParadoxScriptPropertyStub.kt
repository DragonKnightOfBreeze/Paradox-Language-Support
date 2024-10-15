package icu.windea.pls.script.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.model.*

interface ParadoxScriptPropertyStub : ParadoxScriptDefinitionElementStub<ParadoxScriptProperty> {
    abstract class Base(
        parent: StubElement<*>
    ) : StubBase<ParadoxScriptProperty>(parent, ParadoxScriptStubElementTypes.PROPERTY), ParadoxScriptPropertyStub {
        override fun toString(): String {
            return "ParadoxScriptPropertyStub(name=$name, type=$type, subtypes=$subtypes, rootKey=$rootKey, elementPath=$elementPath, gameType=$gameType)"
        }
    }

    //12 + 20 + 4 * 6 = 48 -> 56
    class Impl(
        parent: StubElement<*>,
        override val name: String,
        override val type: String,
        override val subtypes: List<String>?,
        override val rootKey: String,
        override val elementPath: ParadoxExpressionPath,
        override val gameType: ParadoxGameType,
    ) : Base(parent)

    //12 + 20 = 32 -> 32
    class Dummy(
        parent: StubElement<*>,
    ) : Base(parent) {
        override val name: String get() = ""
        override val type: String get() = ""
        override val subtypes: List<String>? get() = null
        override val rootKey: String get() = ""
        override val elementPath: ParadoxExpressionPath get() = ParadoxExpressionPath.Empty
        override val gameType: ParadoxGameType get() = ParadoxGameType.placeholder()

        override fun toString(): String {
            return "ParadoxScriptPropertyStub.Dummy"
        }
    }
}

