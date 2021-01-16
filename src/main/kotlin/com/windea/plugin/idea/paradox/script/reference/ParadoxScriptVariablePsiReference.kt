package com.windea.plugin.idea.paradox.script.reference

import com.intellij.codeInsight.lookup.*
import com.intellij.model.psi.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.psi.*

@Suppress("UnstableApiUsage")
class ParadoxScriptVariablePsiReference(
	element: ParadoxScriptVariableReference,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxScriptVariableReference>(element, rangeInElement), PsiPolyVariantReference, PsiCompletableReference {
	private val project = element.project
	private val file = element.containingFile
	
	override fun handleElementRename(newElementName: String): PsiElement {
		return element.setName(newElementName)
	}
	
	override fun resolve(): ParadoxScriptVariable? {
		//首先尝试从当前文件中查找引用，然后从全局范围中查找引用
		val name = element.variableReferenceId.text
		return findScriptVariableInFile(name, file)
		       ?: findScriptVariable(name, project)
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		//首先尝试从当前文件中查找引用，然后从全局范围中查找引用
		val name = element.variableReferenceId.text
		return findScriptVariablesInFile(name,file)
			.ifEmpty { findScriptVariables(name, project) }
			.mapArray { PsiElementResolveResult(it) }
	}
	
	//注意要传入elementName而非element
	override fun getVariants(): Array<out Any> {
		//同时需要同时查找当前文件中的和全局的
		return (findScriptVariablesInFile(file) + findScriptVariables(project)).mapArray {
			LookupElementBuilder.create(it).withIcon(scriptVariableIcon).withTypeText(it.containingFile.name)
		}
	}
	
	override fun getCompletionVariants(): Collection<LookupElement> {
		//同时需要同时查找当前文件中的和全局的
		return (findScriptVariablesInFile(file) + findScriptVariables(project)).map {
			LookupElementBuilder.create(it).withIcon(scriptVariableIcon).withTypeText(it.containingFile.name)
		}
	}
}
