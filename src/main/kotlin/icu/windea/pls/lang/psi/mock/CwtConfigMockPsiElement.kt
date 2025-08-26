package icu.windea.pls.lang.psi.mock

import com.intellij.lang.*
import com.intellij.psi.*
import icu.windea.pls.cwt.*
import icu.windea.pls.model.*

abstract class CwtConfigMockPsiElement(parent: PsiElement) : MockPsiElement(parent) {
    override fun getLanguage(): Language {
        return CwtLanguage
    }

    abstract val gameType: ParadoxGameType
}
