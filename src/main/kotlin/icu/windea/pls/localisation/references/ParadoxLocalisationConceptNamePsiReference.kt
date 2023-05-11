package icu.windea.pls.localisation.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import com.intellij.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

class ParadoxLocalisationConceptNamePsiReference(
    element: ParadoxLocalisationConceptName,
    rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationConceptName>(element, rangeInElement) {
    val project by lazy { element.project }
    
    override fun handleElementRename(newElementName: String): PsiElement {
        //使用别名时不允许重命名
        if(resolve()?.name != element.name) {
            throw IncorrectOperationException()
        }
        //重命名当前元素
        return element.setName(newElementName)
    }
    
    //缓存解析结果以优化性能
    
    override fun resolve(): ParadoxScriptDefinitionElement? {
        return ResolveCache.getInstance(project).resolveWithCaching(this, Resolver, false, false)
    }
    
    private fun doResolve(): ParadoxScriptDefinitionElement? {
        val element = element
        val nameOrAlias = element.name
        return ParadoxGameConceptHandler.get(nameOrAlias, project, element)
    }
    
    private object Resolver : ResolveCache.AbstractResolver<ParadoxLocalisationConceptNamePsiReference, ParadoxScriptDefinitionElement> {
        override fun resolve(ref: ParadoxLocalisationConceptNamePsiReference, incompleteCode: Boolean): ParadoxScriptDefinitionElement? {
            return ref.doResolve()
        }
    }
}