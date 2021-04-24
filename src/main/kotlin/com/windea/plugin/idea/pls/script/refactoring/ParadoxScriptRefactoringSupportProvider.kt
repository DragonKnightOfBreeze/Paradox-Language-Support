package com.windea.plugin.idea.pls.script.refactoring

import com.intellij.lang.refactoring.*
import com.intellij.psi.*
import com.windea.plugin.idea.pls.script.psi.*

class ParadoxScriptRefactoringSupportProvider : RefactoringSupportProvider() {
	override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
		return element is ParadoxScriptVariable || element is ParadoxScriptProperty
	}
}
