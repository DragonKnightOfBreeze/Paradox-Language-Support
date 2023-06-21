package icu.windea.pls.script.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.cwt.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.expression.ParadoxPathReferenceExpressionSupport.INSTANCE.get
import icu.windea.pls.script.psi.*

/**
 * @see icu.windea.pls.script.codeInsight.completion.ParadoxDefinitionCompletionProvider
 */
class ParadoxScriptExpressionPsiReference(
    element: ParadoxScriptExpressionElement,
    rangeInElement: TextRange,
    val config: CwtConfig<*>,
    val isKey: Boolean?
) : PsiPolyVariantReferenceBase<ParadoxScriptExpressionElement>(element, rangeInElement), PsiReferencesAware {
    val project by lazy { config.info.configGroup.project }
    //val project by lazy { element.project }
    
    override fun handleElementRename(newElementName: String): PsiElement {
        val resolved = resolve()
        return when {
            resolved == null -> element.setValue(rangeInElement.replace(element.value, newElementName))
            resolved is PsiFileSystemItem -> {
                // #33
                val configExpression = config.expression ?: throw IncorrectOperationException()
                val ep = get(configExpression) ?: throw IncorrectOperationException()
                val fileInfo = resolved.fileInfo ?: throw IncorrectOperationException()
                val newFilePath = fileInfo.path.parent + "/" + newElementName
                val pathReference = ep.extract(configExpression, element, newFilePath) ?: throw IncorrectOperationException()
                element.setValue(pathReference)
            }
            resolved.language == CwtLanguage -> throw IncorrectOperationException() //cannot rename cwt config
            resolved.language.isParadoxLanguage() -> element.setValue(rangeInElement.replace(element.value, newElementName))
            else -> throw IncorrectOperationException()
        }
    }
    
    override fun isReferenceTo(element: PsiElement): Boolean {
        //必要的处理，否则查找使用时会出现问题（输入的PsiElement永远不会是propertyKey，只会是property）
        //直接调用resolve()即可
        val resolved = resolve()
        val manager = element.manager
        return manager.areElementsEquivalent(resolved, element) || (resolved is ParadoxScriptProperty && manager.areElementsEquivalent(resolved.propertyKey, element))
    }
    
    override fun getReferences(): Array<out PsiReference>? {
        return ParadoxConfigHandler.getReferences(element, rangeInElement, config, config.expression, config.info.configGroup, isKey)
    }
    
    //缓存解析结果以优化性能
    
    private object Resolver : ResolveCache.AbstractResolver<ParadoxScriptExpressionPsiReference, PsiElement> {
        override fun resolve(ref: ParadoxScriptExpressionPsiReference, incompleteCode: Boolean): PsiElement? {
            return ref.doResolve()
        }
    }
    
    private object MultiResolver : ResolveCache.PolyVariantResolver<ParadoxScriptExpressionPsiReference> {
        override fun resolve(ref: ParadoxScriptExpressionPsiReference, incompleteCode: Boolean): Array<out ResolveResult> {
            return ref.doMultiResolve()
        }
    }
    
    override fun resolve(): PsiElement? {
        return ResolveCache.getInstance(project).resolveWithCaching(this, Resolver, false, false)
    }
    
    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        return ResolveCache.getInstance(project).resolveWithCaching(this, MultiResolver, false, false)
    }
    
    private fun doResolve(): PsiElement? {
        //根据对应的expression进行解析
        return ParadoxConfigHandler.resolveScriptExpression(element, rangeInElement, config, config.expression, config.info.configGroup, isKey)
    }
    
    private fun doMultiResolve(): Array<out ResolveResult> {
        //根据对应的expression进行解析
        return ParadoxConfigHandler.multiResolveScriptExpression(element, rangeInElement, config, config.expression, config.info.configGroup, isKey)
            .mapToArray { PsiElementResolveResult(it) }
    }
}

