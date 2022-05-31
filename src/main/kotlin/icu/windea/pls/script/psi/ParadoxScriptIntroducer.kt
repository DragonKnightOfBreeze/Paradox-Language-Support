package icu.windea.pls.script.psi

import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import org.jetbrains.kotlin.idea.util.*

object ParadoxScriptIntroducer {
	fun introduceLocalScriptedVariable(
		name: String,
		value: String,
		parentDefinition: ParadoxScriptProperty,
		project: Project
	): ParadoxScriptVariable {
		val (parent, anchor) = parentDefinition.findParentAndAnchorToIntroduceVariable()
		var newVariable = ParadoxScriptElementFactory.createVariable(project, name, value)
		val newLine = ParadoxScriptElementFactory.createLine(project)
		newVariable = parent.addAfter(newVariable, anchor).cast()
		if(anchor != null) parent.addBefore(newLine, newVariable) else parent.addAfter(newLine, newVariable)
		return newVariable.reformatted().cast()
	}
	
	private fun ParadoxScriptProperty.findParentAndAnchorToIntroduceVariable(): Pair<PsiElement, PsiElement?> {
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