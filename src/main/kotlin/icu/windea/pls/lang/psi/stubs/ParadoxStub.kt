package icu.windea.pls.lang.psi.stubs

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import icu.windea.pls.model.ParadoxGameType

interface ParadoxStub<T : PsiElement> : StubElement<T> {
    val gameType: ParadoxGameType
}
