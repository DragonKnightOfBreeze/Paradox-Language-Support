package icu.windea.pls.localisation.references

import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.cwt.*
import icu.windea.pls.ep.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.ParadoxExpressionManager.getExpressionText
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationExpressionPsiReference(
    element: ParadoxLocalisationExpressionElement,
    rangeInElement: TextRange
) : PsiPolyVariantReferenceBase<ParadoxLocalisationExpressionElement>(element, rangeInElement), PsiReferencesAware {
    val project by lazy { element.project }

    override fun handleElementRename(newElementName: String): PsiElement {
        val resolved = resolve()
        return when {
            resolved == null -> element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
            resolved.language is CwtLanguage -> throw IncorrectOperationException() //cannot rename cwt config
            resolved.language is ParadoxBaseLanguage -> element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
            else -> throw IncorrectOperationException()
        }
    }

    override fun getReferences(): Array<out PsiReference>? {
        ProgressManager.checkCanceled()
        val expressionText = getExpressionText(element, rangeInElement)

        val result = ParadoxLocalisationExpressionSupport.getReferences(element, rangeInElement, expressionText)
        return result.orNull()
    }

    //缓存解析结果以优化性能

    private object Resolver : ResolveCache.AbstractResolver<ParadoxLocalisationExpressionPsiReference, PsiElement> {
        override fun resolve(ref: ParadoxLocalisationExpressionPsiReference, incompleteCode: Boolean): PsiElement? {
            return ref.doResolve()
        }
    }

    private object MultiResolver : ResolveCache.PolyVariantResolver<ParadoxLocalisationExpressionPsiReference> {
        override fun resolve(ref: ParadoxLocalisationExpressionPsiReference, incompleteCode: Boolean): Array<out ResolveResult> {
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
        return ParadoxExpressionManager.resolveLocalisationExpression(element, rangeInElement)
    }

    private fun doMultiResolve(): Array<out ResolveResult> {
        //根据对应的expression进行解析
        return ParadoxExpressionManager.multiResolveLocalisationExpression(element, rangeInElement)
            .mapToArray { PsiElementResolveResult(it) }
    }
}
