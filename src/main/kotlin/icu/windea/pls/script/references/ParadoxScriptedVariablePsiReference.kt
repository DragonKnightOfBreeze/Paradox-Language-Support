package icu.windea.pls.script.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.collections.ProcessEntry.end
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.script.psi.*

class ParadoxScriptedVariablePsiReference(
	element: ParadoxScriptedVariableReference,
	rangeInElement: TextRange
) : PsiPolyVariantReferenceBase<ParadoxScriptedVariableReference>(element, rangeInElement), SmartPsiReference {
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
		return resolve(true)
	}
	
	override fun resolve(exact: Boolean): ParadoxScriptScriptedVariable? {
		//首先尝试从当前文件中查找引用，然后从全局范围中查找引用
		val element = element
		val name = element.name
		val project = element.project
		val selector = scriptedVariableSelector().gameTypeFrom(element).preferRootFrom(element, exact)
		val localQuery = ParadoxLocalScriptedVariableSearch.search(name, element, selector = selector)
		localQuery.findFirst()?.let { return it }
		val globalQuery = ParadoxGlobalScriptedVariableSearch.search(name, project, selector = selector)
		globalQuery.find(exact)?.let { return it }
		return null
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		//首先尝试从当前文件中查找引用，然后从全局范围中查找引用
		val result = SmartList<ParadoxScriptScriptedVariable>()
		val name = element.name
		val project = element.project
		val selector = scriptedVariableSelector().gameTypeFrom(element).preferRootFrom(element)
		val localQuery = ParadoxLocalScriptedVariableSearch.search(name, element, selector = selector)
		localQuery.processResult { result.add(it).end() }
		val globalQuery = ParadoxGlobalScriptedVariableSearch.search(name, project, selector = selector)
		globalQuery.processResult { result.add(it).end() }
		return result.mapToArray { PsiElementResolveResult(it) }
	}
}
