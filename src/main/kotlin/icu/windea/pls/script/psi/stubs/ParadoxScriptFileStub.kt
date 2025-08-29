package icu.windea.pls.script.psi.stubs

import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.stubs.PsiFileStubImpl
import com.intellij.psi.tree.IElementType
import icu.windea.pls.lang.psi.stubs.ParadoxStub
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptFile

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
