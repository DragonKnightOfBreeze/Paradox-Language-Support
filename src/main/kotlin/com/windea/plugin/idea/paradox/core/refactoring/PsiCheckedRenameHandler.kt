package com.windea.plugin.idea.paradox.core.refactoring

import com.intellij.codeInsight.hint.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.refactoring.rename.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.core.psi.*

class PsiCheckedRenameHandler : PsiElementRenameHandler() {
	override fun isAvailableOnDataContext(dataContext: DataContext): Boolean {
		return getElement(dataContext) is PsiCheckRenameElement
	}
	
	override fun invoke(project: Project, elements: Array<out PsiElement>?, dataContext: DataContext?) {
		val editor = dataContext?.getData(CommonDataKeys.EDITOR)
		if(elements!= null && editor != null) {
			for(element in elements){
				if(element is PsiCheckRenameElement && !check(element,editor)) return
			}
		}
		super.invoke(project, elements, dataContext)
	}
	
	override fun invoke(project: Project, editor: Editor?, file: PsiFile?, dataContext: DataContext) {
		val element = dataContext.findElement()
		if(element is PsiCheckRenameElement && editor != null){
			if(check(element, editor)) super.invoke(project, editor, file, dataContext)
		}else {
			super.invoke(project, editor, file, dataContext)
		}
	}
	
	private fun check(element: PsiCheckRenameElement,editor: Editor):Boolean{
		try{
			element.checkRename()
			return true
		}catch(e:Exception){
			val message = e.message?: return false
			HintManager.getInstance().showErrorHint(editor, message)
			return false
		}
	}
	
	private fun showErrorHint(editor: Editor,message:String) {
		HintManager.getInstance().showErrorHint(editor, message)
	}
}