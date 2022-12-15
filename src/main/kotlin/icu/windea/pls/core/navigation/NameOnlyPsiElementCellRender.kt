package icu.windea.pls.core.navigation

import com.intellij.ide.util.*
import com.intellij.psi.*

class NameOnlyPsiElementCellRender: DefaultPsiElementCellRenderer() {
	override fun getContainerText(element: PsiElement?, name: String?): String? {
		return null
	}
}