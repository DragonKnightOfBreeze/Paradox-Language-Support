package icu.windea.pls.script.psi.stubs

import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.stubs.PsiFileStubImpl
import com.intellij.psi.tree.IElementType
import icu.windea.pls.lang.psi.stubs.ParadoxStub
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.script.psi.ParadoxScriptFile

@Suppress("UnstableApiUsage")
/**
 * 脚本文件存根。
 *
 * - 持有 `gameType`，供其子存根复用。
 */
interface ParadoxScriptFileStub : PsiFileStub<ParadoxScriptFile>, ParadoxStub<ParadoxScriptFile> {
    /**
     * 实现。
     *
     * - `gameType` 直接存储于文件存根。
     */
    class Impl(
        file: ParadoxScriptFile?,
        override val gameType: ParadoxGameType,
    ) : PsiFileStubImpl<ParadoxScriptFile>(file), ParadoxScriptFileStub {
        override fun getParentStub(): ParadoxStub<*>? {
            return null
        }

        override fun getFileElementType(): IElementType {
            return ParadoxScriptFile.ELEMENT_TYPE
        }

        override fun toString(): String {
            return "ParadoxScriptFileStub(gameType=$gameType)"
        }
    }
}
