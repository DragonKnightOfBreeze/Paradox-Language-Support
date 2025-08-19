package icu.windea.pls.script.psi.stubs

import com.intellij.psi.stubs.*
import com.intellij.psi.tree.*
import icu.windea.pls.lang.psi.stubs.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

@Suppress("UnstableApiUsage")
interface ParadoxScriptFileStub : PsiFileStub<ParadoxScriptFile>, ParadoxStub<ParadoxScriptFile> {
    class Impl(
        file: ParadoxScriptFile?,
        override val gameType: ParadoxGameType
    ) : PsiFileStubImpl<ParadoxScriptFile>(file), ParadoxScriptFileStub {
        override fun getFileElementType(): IElementType {
            return ParadoxScriptFile.ELEMENT_TYPE
        }

        override fun toString(): String {
            return "ParadoxScriptFileStub(gameType=$gameType)"
        }
    }
}
