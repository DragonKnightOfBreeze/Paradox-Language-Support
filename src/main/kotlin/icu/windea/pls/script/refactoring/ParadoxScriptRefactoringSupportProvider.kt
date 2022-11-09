package icu.windea.pls.script.refactoring

import com.intellij.lang.refactoring.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.*

//代码重构：
//重命名：变量，属性（仅限名字来自rootKey的定义），表达式
//安全删除：变量 TODO

class ParadoxScriptRefactoringSupportProvider : RefactoringSupportProvider() {
	override fun isMemberInplaceRenameAvailable(element: PsiElement, context: PsiElement?): Boolean {
		if(element is ParadoxScriptScriptedVariable) return true
		if(element is ParadoxScriptProperty && element.definitionInfo.let { it != null && it.typeConfig.nameField == null }) return true
		if(element is ParadoxExpressionElement) return true
		return false
	}
}
