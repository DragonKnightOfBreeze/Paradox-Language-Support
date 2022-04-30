package icu.windea.pls.script.reference

import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

class ParadoxScriptVariableReferenceReference(
	element: ParadoxScriptVariableReference,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxScriptVariableReference>(element, rangeInElement), PsiPolyVariantReference {
	override fun handleElementRename(newElementName: String): PsiElement {
		//尝试重命名关联的variable
		val resolved = resolve()
		when {
			resolved == null -> pass()
			!resolved.isWritable -> throw IncorrectOperationException(PlsBundle.message("cannotBeRenamed"))
			else -> resolved.name = newElementName
		}
		//重命名variableReference
		return element.setName(newElementName)
	}
	
	override fun resolve(): ParadoxScriptVariable? {
		//首先尝试从当前文件中查找引用，然后从全局范围中查找引用
		val name = element.name
		val project = element.project
		return findScriptVariableInFile(name, element)
			?: findScriptVariable(name, project)
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		//首先尝试从当前文件中查找引用，然后从全局范围中查找引用
		val name = element.name
		val project = element.project
		val file = element.containingFile
		return findScriptVariablesInFile(name, element)
			.ifEmpty { findScriptVariables(name, project) }
			.mapToArray { PsiElementResolveResult(it) }
	}
	
	override fun getVariants(): Array<out Any> {
		//同时需要同时查找当前文件中的和全局的
		val project = element.project
		return (findAllScriptVariablesInFile(element) + findAllScriptVariables(project, distinct = true))
			.distinctBy { it.name } //这里还要进行一次去重
			.mapToArray {
				val name = it.name
				val icon = it.icon
				val typeText = it.containingFile.name
				LookupElementBuilder.create(it, name).withIcon(icon).withTypeText(typeText, true)
			}
	}
}
