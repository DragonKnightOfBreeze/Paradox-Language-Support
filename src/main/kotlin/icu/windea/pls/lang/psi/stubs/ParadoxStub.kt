package icu.windea.pls.lang.psi.stubs

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import icu.windea.pls.model.ParadoxGameType

/**
 * 存根的顶级接口。
 *
 * @property gameType 游戏类型。文件的存根应该直接存储游戏类型，非文件的存根则应当从父存根获取。
 */
interface ParadoxStub<T : PsiElement> : StubElement<T> {
    val gameType: ParadoxGameType

    override fun getParentStub(): ParadoxStub<*>?
}
