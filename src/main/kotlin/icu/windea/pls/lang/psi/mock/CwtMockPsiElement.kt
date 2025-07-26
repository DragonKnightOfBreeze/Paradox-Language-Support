package icu.windea.pls.lang.psi.mock

import com.intellij.lang.*
import com.intellij.psi.*
import icu.windea.pls.cwt.*

abstract class CwtMockPsiElement(parent: PsiElement) : MockPsiElement(parent) {
    override fun getLanguage(): Language {
        return CwtLanguage
    }
}
