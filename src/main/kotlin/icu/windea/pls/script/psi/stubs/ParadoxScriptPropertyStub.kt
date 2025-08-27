package icu.windea.pls.script.psi.stubs

import com.intellij.psi.stubs.*
import icu.windea.pls.lang.psi.stubs.*
import icu.windea.pls.model.*
import icu.windea.pls.model.paths.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*

@Suppress("UnstableApiUsage")
interface ParadoxScriptPropertyStub : ParadoxStub<ParadoxScriptProperty> {
    val definitionName: String
    val definitionType: String
    val definitionSubtypes: List<String>? //null -> 无法在索引时获取（需要访问定义索引）
    val rootKey: String
    val elementPath: ParadoxExpressionPath

    val isValidDefinition: Boolean get() = definitionType.isNotEmpty()

    abstract class Base(
        parent: StubElement<*>?
    ) : StubBase<ParadoxScriptProperty>(parent, PROPERTY), ParadoxScriptPropertyStub {
        override fun toString(): String {
            return "ParadoxScriptPropertyStub(definitionName=$definitionName, definitionType=$definitionType, gameType=$gameType)"
        }
    }

    class Impl(
        parent: StubElement<*>?,
        override val definitionName: String,
        override val definitionType: String,
        override val definitionSubtypes: List<String>?,
        override val rootKey: String,
        override val elementPath: ParadoxExpressionPath,
        override val gameType: ParadoxGameType,
    ) : Base(parent)

    class Dummy(
        parent: StubElement<*>?,
    ) : Base(parent) {
        override val definitionName: String get() = ""
        override val definitionType: String get() = ""
        override val definitionSubtypes: List<String>? get() = null
        override val rootKey: String get() = ""
        override val elementPath: ParadoxExpressionPath get() = ParadoxExpressionPath.resolveEmpty()
        override val gameType: ParadoxGameType get() = ParadoxGameType.placeholder()

        override fun toString(): String {
            return "ParadoxScriptPropertyStub.Dummy"
        }
    }
}

