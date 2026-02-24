package icu.windea.pls.lang.psi.light

import com.intellij.psi.PsiElement
import icu.windea.pls.model.ParadoxGameType

abstract class ParadoxMockPsiElement(parent: PsiElement) : MockPsiElement(parent) {
    // override fun getLanguage(): Language {
    //    return ParadoxLanguage
    // }

    abstract val gameType: ParadoxGameType
}
