package com.windea.plugin.idea.paradox.localisation.refactoring

import com.intellij.lang.refactoring.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.localisation.psi.*

//代码重构：
//重命名：属性
//安全删除：属性

class ParadoxLocalisationRefactoringSupportProvider : RefactoringSupportProvider() {
	override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
		return element is ParadoxLocalisationProperty
	}
}
