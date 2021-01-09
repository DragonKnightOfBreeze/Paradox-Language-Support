package com.windea.plugin.idea.paradox.script.reference

import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.psi.*

class ParadoxScriptVariablePsiReference(
	element: ParadoxScriptVariableReference,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxScriptVariableReference>(element, rangeInElement), PsiPolyVariantReference {
	private val name = rangeInElement.substring(element.text)
	private val project = element.project
	private val scope = element.resolveScope
	
	override fun handleElementRename(newElementName: String): PsiElement {
		return element.setName(newElementName)
	}
	
	override fun resolve(): ParadoxScriptVariable? {
		val file = element.containingFile ?: return null
		//首先尝试从当前文档中查找引用
		findScriptVariableInFile(name, file)?.let { return it }
		//然后从全局范围中查找引用
		return findScriptVariable(name, project, scope)
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		//首先尝试从当前文档中查找引用
		resolve()?.let { return arrayOf(PsiElementResolveResult(it)) }
		//然后从全局范围中查找引用
		return findScriptVariables(name, project, scope).mapArray { PsiElementResolveResult(it) }
	}
	
	override fun getVariants(): Array<out Any> {
		return findScriptVariables(project, scope).mapArray {
			LookupElementBuilder.create(it.name).withIcon(it.getIcon(0)).withTypeText(it.containingFile.name).withPsiElement(it)
		}
	}
}
