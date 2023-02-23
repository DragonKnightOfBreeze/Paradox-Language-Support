package icu.windea.pls.core.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selectors.chained.*
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
        val selector = scriptedVariableSelector(project, element).contextSensitive(exact)
        ParadoxLocalScriptedVariableSearch.search(name, element, selector).findFirst()?.let { return it }
        ParadoxGlobalScriptedVariableSearch.search(name, selector).find(exact)?.let { return it }
        return null
    }
    
    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        //首先尝试从当前文件中查找引用，然后从全局范围中查找引用
        val element = element
        val result = SmartList<ParadoxScriptScriptedVariable>()
        val name = element.name
        val project = element.project
        val selector = scriptedVariableSelector(project, element).contextSensitive()
        ParadoxLocalScriptedVariableSearch.search(name, element, selector).processQuery {
            result.add(it)
            true
        }
        ParadoxGlobalScriptedVariableSearch.search(name, selector).processQuery {
            result.add(it)
            true
        }
        return result.mapToArray { PsiElementResolveResult(it) }
    }
}
