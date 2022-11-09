package icu.windea.pls.script.reference

import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*

class ParadoxScriptedVariableReference(
	element: IParadoxScriptVariableReference,
	rangeInElement: TextRange
) : PsiPolyVariantReferenceBase<IParadoxScriptVariableReference>(element, rangeInElement) {
	override fun handleElementRename(newElementName: String): PsiElement {
		//尝试重命名关联的variable
		val resolved = resolve()
		when {
			resolved == null -> pass()
			else -> resolved.name = newElementName
		}
		//重命名variableReference
		return element.setName(newElementName)
	}
	
	override fun resolve(): ParadoxScriptScriptedVariable? {
		//首先尝试从当前文件中查找引用，然后从全局范围中查找引用
		val name = element.name
		val project = element.project
		return findScriptedVariableInFile(name, element)
			?: findScriptedVariable(name, project, selector = scriptedVariableSelector().gameTypeFrom(element).preferRootFrom(element))
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		//首先尝试从当前文件中查找引用，然后从全局范围中查找引用
		val name = element.name
		val project = element.project
		return findScriptedVariablesInFile(name, element)
			.ifEmpty { findScriptedVariables(name, project, selector = scriptedVariableSelector().gameTypeFrom(element).preferRootFrom(element)) }
			.mapToArray { PsiElementResolveResult(it) }
	}
	
	override fun getVariants(): Array<out Any> {
		//同时需要同时查找当前文件中的和全局的
		val project = element.project
		return findAllScriptVariablesInFile(element, distinct = true)
			.plus(findAllScriptedVariables(project, distinct = true, selector = scriptedVariableSelector().gameTypeFrom(element).preferRootFrom(element)))
			.distinctBy { it.name } //这里还要进行一次去重
			.mapToArray {
				val name = it.name
				val icon = it.icon
				val typeText = it.containingFile.name
				LookupElementBuilder.create(it, name).withIcon(icon).withTypeText(typeText, true)
			}
	}
}