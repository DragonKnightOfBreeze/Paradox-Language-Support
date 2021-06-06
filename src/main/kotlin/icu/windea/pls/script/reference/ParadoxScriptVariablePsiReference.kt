package icu.windea.pls.script.reference

import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

class ParadoxScriptVariablePsiReference(
	element: ParadoxScriptVariableReference,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxScriptVariableReference>(element, rangeInElement), PsiPolyVariantReference {
	override fun handleElementRename(newElementName: String): PsiElement {
		return element.setName(newElementName)
	}
	
	override fun resolve(): ParadoxScriptVariable? {
		//首先尝试从当前文件中查找引用，然后从全局范围中查找引用
		val name = element.variableReferenceId.text
		val project = element.project
		val file = element.containingFile
		return findScriptVariableInFile(name, file)
		       ?: findScriptVariable(name, project)
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		//首先尝试从当前文件中查找引用，然后从全局范围中查找引用
		val name = element.variableReferenceId.text
		val project = element.project
		val file = element.containingFile
		return findScriptVariablesInFile(name,file)
			.ifEmpty { findScriptVariables(name, project) }
			.mapToArray { PsiElementResolveResult(it) }
	}
	
	override fun getVariants(): Array<out Any> {
		val project = element.project
		val file = element.containingFile
		//同时需要同时查找当前文件中的和全局的
		return (findScriptVariablesInFile(file) + findScriptVariables(project)).mapToArray {
			val name = it.name
			val icon = scriptVariableIcon
			val filePath = it.containingFile.virtualFile.path
			LookupElementBuilder.create(it,name).withIcon(icon).withTailText(filePath,true)
		}
	}
}
