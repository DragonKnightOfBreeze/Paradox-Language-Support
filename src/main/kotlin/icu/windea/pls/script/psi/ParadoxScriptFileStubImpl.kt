package icu.windea.pls.script.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.model.*

class ParadoxScriptFileStubImpl(
    file: ParadoxScriptFile?,
    override val name: String,
    override val type: String,
    override val subtypes: List<String>?,
    override val gameType: ParadoxGameType
) : PsiFileStubImpl<ParadoxScriptFile>(file), ParadoxScriptFileStub {
    override val elementPath: ParadoxExpressionPath get() = ParadoxExpressionPath.Empty
    override val rootKey: String get() = name

    override fun toString(): String {
        return "ParadoxScriptFileStub(name=$name, type=$type, subtypes=$subtypes, rootKey=$rootKey, gameType=$gameType)"
    }
}
