package icu.windea.pls.lang.references

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import icu.windea.pls.core.collections.mapToArray
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference
import icu.windea.pls.lang.search.ParadoxGlobalScriptedVariableSearch
import icu.windea.pls.lang.search.ParadoxLocalScriptedVariableSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.scriptedVariable
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

class ParadoxScriptedVariablePsiReference(
    element: ParadoxScriptedVariableReference,
    rangeInElement: TextRange
) : PsiPolyVariantReferenceBase<ParadoxScriptedVariableReference>(element, rangeInElement) {
    val project by lazy { element.project }

    override fun handleElementRename(newElementName: String): PsiElement {
        return element.setName(newElementName)
    }

    //缓存解析结果以优化性能

    private object Resolver : ResolveCache.AbstractResolver<ParadoxScriptedVariablePsiReference, ParadoxScriptScriptedVariable> {
        override fun resolve(ref: ParadoxScriptedVariablePsiReference, incompleteCode: Boolean) = ref.doResolve()
    }

    private object MultiResolver : ResolveCache.PolyVariantResolver<ParadoxScriptedVariablePsiReference> {
        override fun resolve(ref: ParadoxScriptedVariablePsiReference, incompleteCode: Boolean) = ref.doMultiResolve()
    }

    override fun resolve(): ParadoxScriptScriptedVariable? {
        return ResolveCache.getInstance(project).resolveWithCaching(this, Resolver, false, false)
    }

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        return ResolveCache.getInstance(project).resolveWithCaching(this, MultiResolver, false, false)
    }

    private fun doResolve(): ParadoxScriptScriptedVariable? {
        //首先尝试从当前文件中查找引用，然后从全局范围中查找引用
        val element = element
        val name = element.name ?: return null
        val selector = selector(project, element).scriptedVariable().contextSensitive()
        ParadoxLocalScriptedVariableSearch.search(name, selector).find()?.let { return it }
        ParadoxGlobalScriptedVariableSearch.search(name, selector).find()?.let { return it }
        return null
    }

    private fun doMultiResolve(): Array<out ResolveResult> {
        //首先尝试从当前文件中查找引用，然后从全局范围中查找引用
        val element = element
        val name = element.name ?: return ResolveResult.EMPTY_ARRAY
        val result = mutableListOf<ParadoxScriptScriptedVariable>()
        val selector = selector(project, element).scriptedVariable().contextSensitive()
        ParadoxLocalScriptedVariableSearch.search(name, selector).findAll().let { result += it }
        ParadoxGlobalScriptedVariableSearch.search(name, selector).findAll().let { result += it }
        return result.mapToArray { PsiElementResolveResult(it) }
    }
}
