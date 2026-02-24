package icu.windea.pls.lang.psi.light

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.model.ParadoxGameType

abstract class CwtConfigMockPsiElement(parent: PsiElement) : MockPsiElement(parent) {
    override fun getLanguage(): Language {
        return CwtLanguage
    }

    abstract val gameType: ParadoxGameType
}
