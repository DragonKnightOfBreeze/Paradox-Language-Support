package icu.windea.pls.lang.psi.mock

import com.intellij.psi.*
import icu.windea.pls.model.*

abstract class ParadoxMockPsiElement(parent: PsiElement) : MockPsiElement(parent) {
    //override fun getLanguage(): Language {
    //    return ParadoxBaseLanguage
    //}

    abstract val gameType: ParadoxGameType
}
