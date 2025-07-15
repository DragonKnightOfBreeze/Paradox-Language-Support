package icu.windea.pls.lang.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

class ParadoxTemplateSnippetPsiExpressionReference(
    element: ParadoxScriptStringExpressionElement,
    rangeInElement: TextRange,
    val name: String,
    val configExpression: CwtDataExpression,
    val configGroup: CwtConfigGroup
) : PsiPolyVariantReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement) {
    val project by lazy { configGroup.project }

    val config = CwtValueConfig.resolve(emptyPointer(), configGroup, configExpression.expressionString)

    override fun handleElementRename(newElementName: String): PsiElement {
        throw IncorrectOperationException() //cannot rename template snippet
    }

    //缓存解析结果以优化性能

    private object Resolver : ResolveCache.AbstractResolver<ParadoxTemplateSnippetPsiExpressionReference, PsiElement> {
        override fun resolve(ref: ParadoxTemplateSnippetPsiExpressionReference, incompleteCode: Boolean) = ref.doResolve()
    }

    private object MultiResolver : ResolveCache.PolyVariantResolver<ParadoxTemplateSnippetPsiExpressionReference> {
        override fun resolve(ref: ParadoxTemplateSnippetPsiExpressionReference, incompleteCode: Boolean) = ref.doMultiResolve()
    }

    override fun resolve(): PsiElement? {
        return ResolveCache.getInstance(project).resolveWithCaching(this, Resolver, false, false)
    }

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        return ResolveCache.getInstance(project).resolveWithCaching(this, MultiResolver, false, false)
    }

    private fun doResolve(): PsiElement? {
        val element = element
        return ParadoxExpressionManager.resolveScriptExpression(element, rangeInElement, config, configExpression)
    }

    private fun doMultiResolve(): Array<out ResolveResult> {
        val element = element
        return ParadoxExpressionManager.multiResolveScriptExpression(element, rangeInElement, config, configExpression)
            .mapToArray { PsiElementResolveResult(it) }
    }
}
