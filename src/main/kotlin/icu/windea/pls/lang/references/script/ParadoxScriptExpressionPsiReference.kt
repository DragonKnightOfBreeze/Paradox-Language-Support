package icu.windea.pls.lang.references.script

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.PsiReference
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.createResults
import icu.windea.pls.core.psi.PsiCompositeReference
import icu.windea.pls.lang.psi.ParadoxPsiService
import icu.windea.pls.lang.resolve.ParadoxExpressionService
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxExpressionManager.getExpressionText
import icu.windea.pls.lang.util.ParadoxTagManager
import icu.windea.pls.model.type.ParadoxExpressionRole
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey

/**
 * @see icu.windea.pls.lang.codeInsight.completion.script.ParadoxScriptExpressionCompletionProvider
 */
class ParadoxScriptExpressionPsiReference(
    element: ParadoxScriptExpressionElement,
    rangeInElement: TextRange,
    val configs: List<CwtMemberConfig<*>>,
    val role: ParadoxExpressionRole,
) : PsiPolyVariantReferenceBase<ParadoxScriptExpressionElement>(element, rangeInElement), PsiCompositeReference, ParadoxScriptTagAwarePsiReference {
    val config: CwtMemberConfig<*> get() = configs.first()

    private val configGroup get() = configs.first().configGroup
    private val project get() = configGroup.project

    override val tagConfig: CwtValueConfig? get() = config.castOrNull()

    init {
        ParadoxTagManager.processConfigs(configs)
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        return ParadoxPsiService.handleExpressionElementRename(element, rangeInElement, newElementName, resolve(), config.configExpression)
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        // 兼容性处理（property VS propertyKey）
        if (element is ParadoxScriptPropertyKey && isReferenceTo(element.parent)) return true
        return super.isReferenceTo(element)
    }

    override fun getReferences(): List<PsiReference> {
        val expressionText = getExpressionText(element, rangeInElement)
        val result = ParadoxExpressionService.getScriptExpressionReferences(element, rangeInElement, expressionText, config, role)
        return result
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
        val resolved = configs.firstNotNullOfOrNull { config ->
            ParadoxExpressionManager.resolveScriptExpression(element, rangeInElement, config, role)
        }
        return resolved
    }

    private fun doMultiResolve(): Array<out ResolveResult> {
        // 根据对应的 expression 进行解析
        val resolved = configs.flatMap { config ->
            ParadoxExpressionManager.resolveAllScriptExpression(element, rangeInElement, config, role)
        }
        return resolved.createResults()
    }
}
