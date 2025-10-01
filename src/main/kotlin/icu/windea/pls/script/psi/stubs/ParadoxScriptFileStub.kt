package icu.windea.pls.script.psi.stubs

import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.stubs.PsiFileStubImpl
import icu.windea.pls.lang.psi.stubs.ParadoxStub
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptFile

/**
 * 脚本文件的存根。
 */
@Suppress("UnstableApiUsage")
interface ParadoxScriptFileStub : PsiFileStub<ParadoxScriptFile>, ParadoxStub<ParadoxScriptFile> {
    private class Impl(
        file: ParadoxScriptFile?,
        override val gameType: ParadoxGameType,
    ) : PsiFileStubImpl<ParadoxScriptFile>(file), ParadoxScriptFileStub {
        override fun getParentStub() = null

        override fun getFileElementType() = ParadoxScriptFile.ELEMENT_TYPE

        override fun toString(): String {
            return "ParadoxScriptFileStub(gameType=$gameType)"
        }
    }

    companion object {
        fun create(file: ParadoxScriptFile?, gameType: ParadoxGameType): ParadoxScriptFileStub {
            return Impl(file, gameType)
        }
    }
}
