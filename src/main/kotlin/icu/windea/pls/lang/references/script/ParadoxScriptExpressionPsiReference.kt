package icu.windea.pls.lang.references.script

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.PsiReference
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.config.bindConfig
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.tagType
import icu.windea.pls.core.collections.mapToArray
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.psi.PsiReferencesAware
import icu.windea.pls.core.unquote
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.ep.expression.ParadoxPathReferenceExpressionSupport
import icu.windea.pls.ep.expression.ParadoxScriptExpressionSupport
import icu.windea.pls.lang.ParadoxBaseLanguage
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxExpressionManager.getExpressionText
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey

/**
 * @see icu.windea.pls.lang.codeInsight.completion.script.ParadoxScriptExpressionCompletionProvider
 */
class ParadoxScriptExpressionPsiReference(
    element: ParadoxScriptExpressionElement,
    rangeInElement: TextRange,
    val config: CwtConfig<*>,
    val isKey: Boolean?
) : PsiPolyVariantReferenceBase<ParadoxScriptExpressionElement>(element, rangeInElement), PsiReferencesAware {
    val project by lazy { config.configGroup.project }
    //val project by lazy { element.project }

    init {
        bindConfigForResolved()
    }

    private fun bindConfigForResolved() {
        //用于处理特殊标签
        if (config is CwtValueConfig && config.tagType != null) {
            config.pointer.element?.bindConfig(config)
        }
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        val resolved = resolve()
        return when {
            resolved == null -> element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
            resolved is PsiFileSystemItem -> {
                //https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/#33
                val configExpression = config.configExpression ?: throw IncorrectOperationException()
                val ep = ParadoxPathReferenceExpressionSupport.get(configExpression) ?: throw IncorrectOperationException()
                val fileInfo = resolved.fileInfo ?: throw IncorrectOperationException()
                val newFilePath = fileInfo.path.parent + "/" + newElementName
                val pathReference = ep.extract(configExpression, element, newFilePath) ?: throw IncorrectOperationException()
                element.setValue(pathReference)
            }
            resolved.language is CwtLanguage -> throw IncorrectOperationException() //cannot rename cwt config
            resolved.language is ParadoxBaseLanguage -> element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
            else -> throw IncorrectOperationException()
        }
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        //兼容性处理（property VS propertyKey）
        if (element is ParadoxScriptPropertyKey && isReferenceTo(element.parent)) return true
        return super.isReferenceTo(element)
    }

    override fun getReferences(): Array<out PsiReference>? {
        ProgressManager.checkCanceled()
        if (config.configExpression == null) return null
        val expressionText = getExpressionText(element, rangeInElement)

        val result = ParadoxScriptExpressionSupport.getReferences(element, rangeInElement, expressionText, config, isKey)
        return result.orNull()
    }

    //缓存解析结果以优化性能

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
        //根据对应的expression进行解析
        return ParadoxExpressionManager.resolveScriptExpression(element, rangeInElement, config, config.configExpression, isKey)
    }

    private fun doMultiResolve(): Array<out ResolveResult> {
        //根据对应的expression进行解析
        return ParadoxExpressionManager.multiResolveScriptExpression(element, rangeInElement, config, config.configExpression, isKey)
            .mapToArray { PsiElementResolveResult(it) }
    }
}

