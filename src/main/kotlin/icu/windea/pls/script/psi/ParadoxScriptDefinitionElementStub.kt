package icu.windea.pls.script.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.model.*

interface ParadoxScriptDefinitionElementStub<T : ParadoxScriptDefinitionElement> : StubElement<T> {
    val name: String
    val type: String
    val subtypes: List<String>? //null -> 无法在索引时获取（需要访问定义索引）
    val rootKey: String
    val elementPath: ParadoxExpressionPath
    val gameType: ParadoxGameType

    val isValidDefinition: Boolean get() = type.isNotEmpty()
}
