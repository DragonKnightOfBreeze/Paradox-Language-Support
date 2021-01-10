package com.windea.plugin.idea.paradox.script.reference

import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.psi.*

////TODO scope

class ParadoxScriptVariablePsiReference(
	element: ParadoxScriptVariableReference,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxScriptVariableReference>(element, rangeInElement), PsiPolyVariantReference {
	private val project = element.project
	private val file = element.containingFile
	
	override fun handleElementRename(newElementName: String): PsiElement {
		return element.setName(newElementName)
	}
	
	override fun resolve(): ParadoxScriptVariable? {
		//首先尝试从当前文件中查找引用，然后从全局范围中查找引用
		val name = element.variableReferenceId.text
		return findScriptVariable(name, project, fileScope(file))
		       ?: findScriptVariable(name, project)
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		//首先尝试从当前文件中查找引用，然后从全局范围中查找引用
		val name = element.variableReferenceId.text
		return findScriptVariables(name, project, fileScope(file)).ifEmpty { 
			findScriptVariables(name, project) 
		}.mapArray { PsiElementResolveResult(it) }
	}
	
	//注意要传入elementName而非element
	override fun getVariants(): Array<out Any> {
		//同时需要同时查找当前文件中的和全局的
		return (findScriptVariables(project,fileScope(file)) + findScriptVariables(project)).mapArray {
			LookupElementBuilder.create(it.name).withIcon(it.getIcon(0)).withTypeText(it.containingFile.name).withPsiElement(it)
		}
	}
}
