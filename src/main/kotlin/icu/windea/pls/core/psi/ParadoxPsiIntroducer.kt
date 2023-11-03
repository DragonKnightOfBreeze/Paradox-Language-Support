package icu.windea.pls.core.psi

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.script.psi.*

object ParadoxPsiIntroducer {
	/**
	 * 在所属定义之前另起一行（跳过注释和空白），声明指定名字和值的封装变量。
	 */
	fun introduceLocalScriptedVariable(name: String, value: String, parentDefinitionOrFile: ParadoxScriptDefinitionElement, project: Project): ParadoxScriptScriptedVariable {
		val (parent, anchor) = parentDefinitionOrFile.findParentAndAnchorToIntroduceLocalScriptedVariable()
		var newVariable = ParadoxScriptElementFactory.createScriptedVariable(project, name, value)
		val newLine = ParadoxScriptElementFactory.createLine(project)
		newVariable = parent.addAfter(newVariable, anchor).cast()
		if(anchor != null) parent.addBefore(newLine, newVariable) else parent.addAfter(newLine, newVariable)
		return newVariable
	}
	
	private fun ParadoxScriptDefinitionElement.findParentAndAnchorToIntroduceLocalScriptedVariable(): Pair<PsiElement, PsiElement?> {
		if(this is ParadoxScriptFile) {
			val anchor = this.findChildOfType<ParadoxScriptScriptedVariable>(forward = false)
				?: return this to this.lastChild
			return this to anchor
		} else {
			val parent = parent
			val anchor: PsiElement? = this.siblings(forward = false, withSelf = false).find {
				it !is PsiWhiteSpace && it !is PsiComment
			}
			if(anchor == null && parent is ParadoxScriptRootBlock) {
				return parent.parent to null //(file, null)
			}
			return parent to anchor
		}
	}
	
	/**
	 * 在指定文件的最后一个封装变量声明后或者最后一个PSI元素后另起一行，声明指定名字和值的封装变量。
	 */
	fun introduceGlobalScriptedVariable(name: String, value: String, targetFile: ParadoxScriptFile, project: Project): ParadoxScriptScriptedVariable {
		val (parent, anchor) = targetFile.findParentAndAnchorToIntroduceGlobalScriptedVariable()
		var newVariable = ParadoxScriptElementFactory.createScriptedVariable(project, name, value)
		val newLine = ParadoxScriptElementFactory.createLine(project)
		newVariable = parent.addAfter(newVariable, anchor).cast()
		parent.addBefore(newLine, newVariable)
		return newVariable
	}
	
	private fun ParadoxScriptFile.findParentAndAnchorToIntroduceGlobalScriptedVariable(): Pair<PsiElement, PsiElement> {
		val anchor = this.findChildOfType<ParadoxScriptScriptedVariable>(forward = false)
			?: return this to this.lastChild
		return this to anchor
	}
}
