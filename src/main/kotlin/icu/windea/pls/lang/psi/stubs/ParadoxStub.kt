package icu.windea.pls.lang.psi.stubs

import com.intellij.psi.*
import com.intellij.psi.stubs.*
import icu.windea.pls.model.*

interface ParadoxStub<T : PsiElement> : StubElement<T> {
    val gameType: ParadoxGameType
}
