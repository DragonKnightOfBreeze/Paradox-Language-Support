package icu.windea.pls.lang.navigation

import com.intellij.ide.util.*
import com.intellij.psi.*

class NameOnlyPsiElementCellRender : DefaultPsiElementCellRenderer() {
    override fun getContainerText(element: PsiElement?, name: String?): String? {
        return null
    }
}
