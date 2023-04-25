package icu.windea.pls.expression.psi

import com.intellij.extapi.psi.*
import com.intellij.psi.*
import icu.windea.pls.expression.*

class ParadoxExpressionFile(
	viewProvider: FileViewProvider
) : PsiFileBase(viewProvider, ParadoxExpressionLanguage) {
	override fun getFileType() = ParadoxExpressionFileType
}
