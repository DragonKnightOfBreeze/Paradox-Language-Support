package icu.windea.pls.core.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.script.psi.*

class ParadoxScriptedVariablePsiReference(
	element: ParadoxScriptedVariableReference,
	rangeInElement: TextRange
) : PsiPolyVariantReferenceBase<ParadoxScriptedVariableReference>(element, rangeInElement), PsiNodeReference {
	override fun handleElementRename(newElementName: String): PsiElement {
		//重命名当前元素
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
		localQuery.processQuery {
			result.add(it)
			true
		}
		val globalQuery = ParadoxGlobalScriptedVariableSearch.search(name, project, selector = selector)
		globalQuery.processQuery {
			result.add(it)
			true
		}
		return result.mapToArray { PsiElementResolveResult(it) }
	}
}
