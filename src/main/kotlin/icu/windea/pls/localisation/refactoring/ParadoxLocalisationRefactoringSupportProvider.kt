package icu.windea.pls.localisation.refactoring

import com.intellij.lang.refactoring.*
import com.intellij.psi.*
import icu.windea.pls.localisation.psi.*

//代码重构：
//重命名：属性
//安全删除：属性 TODO

class ParadoxLocalisationRefactoringSupportProvider : RefactoringSupportProvider() {
	override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
		if(element is ParadoxLocalisationProperty) return true
		return false
	}
}
