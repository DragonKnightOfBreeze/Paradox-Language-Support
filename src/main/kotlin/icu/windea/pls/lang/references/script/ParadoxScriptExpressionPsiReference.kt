package icu.windea.pls.lang.references.script

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.PsiReference
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.expandConfigExpression
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.flatMapFast
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.createResults
import icu.windea.pls.core.psi.PsiCompositeReference
import icu.windea.pls.core.util.ProcessorScope
import icu.windea.pls.lang.codeInsight.completion.script.ParadoxScriptExpressionCompletionProvider
import icu.windea.pls.lang.psi.ParadoxPsiService
import icu.windea.pls.lang.references.ParadoxConstrainedPsiReference
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxTagManager
import icu.windea.pls.model.constraints.ParadoxReferenceConstraint
import icu.windea.pls.model.type.ParadoxExpressionRole
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey

/**
 * @see ParadoxScriptPsiReferenceProvider
 * @see ParadoxScriptExpressionCompletionProvider
 */
class ParadoxScriptExpressionPsiReference(
    element: ParadoxScriptExpressionElement,
    rangeInElement: TextRange,
    val configs: List<CwtMemberConfig<*>>,
    val role: ParadoxExpressionRole,
) : PsiPolyVariantReferenceBase<ParadoxScriptExpressionElement>(element, rangeInElement), PsiCompositeReference, ParadoxScriptTagAwarePsiReference, ParadoxConstrainedPsiReference {
    val config: CwtMemberConfig<*> get() = configs.first()

    private val configGroup get() = configs.first().configGroup
    private val project get() = configGroup.project

    override val tagConfig: CwtValueConfig? get() = config.castOrNull()

    init {
        ParadoxTagManager.processConfigs(configs)
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        // 兼容性处理（property VS propertyKey）
        if (element is ParadoxScriptPropertyKey && isReferenceTo(element.parent)) return true
        return super.isReferenceTo(element)
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        return ParadoxPsiService.handleExpressionElementRename(element, rangeInElement, newElementName, resolve(), config.configExpression)
    }

    override fun getReferences(): List<PsiReference> {
        return ParadoxExpressionManager.getScriptExpressionReferences(element, rangeInElement, config, role)
    }

    // 缓存解析结果以优化性能

    private object Resolver : ResolveCache.AbstractResolver<ParadoxScriptExpressionPsiReference, PsiElement> {
        override fun resolve(ref: ParadoxScriptExpressionPsiReference, incompleteCode: Boolean) = ref.doResolve()
    }

    private object MultiResolver : ResolveCache.PolyVariantResolver<ParadoxScriptExpressionPsiReference> {
        override fun resolve(ref: ParadoxScriptExpressionPsiReference, incompleteCode: Boolean) = ref.doMultiResolve()
    }

    override fun resolve(): PsiElement? {
        return ResolveCache.getInstance(project).resolveWithCaching(this, Resolver, false, false)
    }

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        return ResolveCache.getInstance(project).resolveWithCaching(this, MultiResolver, false, false)
    }

    private fun doResolve(): PsiElement? {
        // 根据对应的 expression 进行解析
        val element = element
        val rangeInElement = rangeInElement
        configs.forEachFast { config ->
            ParadoxExpressionManager.resolveScriptExpression(element, rangeInElement, config, role)?.let { return it }
        }
        return null
    }

    private fun doMultiResolve(): Array<out ResolveResult> {
        // 根据对应的 expression 进行解析
        val element = element
        val rangeInElement = rangeInElement
        val resolved = configs.flatMapFast { config ->
            ParadoxExpressionManager.resolveAllScriptExpression(element, rangeInElement, config, role)
        }
        return resolved.createResults()
    }

    override fun canResolveFor(constraint: ParadoxReferenceConstraint): Boolean {
        // NOTE 3.0.1 expand config expression first since it's necessary for unions and aliases
        return ProcessorScope.anyFrom({ configs.expandConfigExpression { process(it) } }) { constraint.test(it.type) }
    }
}
