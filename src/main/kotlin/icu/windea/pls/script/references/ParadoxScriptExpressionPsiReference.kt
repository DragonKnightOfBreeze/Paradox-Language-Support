package icu.windea.pls.script.references

import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.cwt.*
import icu.windea.pls.ep.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.ParadoxExpressionManager.getExpressionText
import icu.windea.pls.script.psi.*

/**
 * @see icu.windea.pls.script.codeInsight.completion.ParadoxScriptExpressionCompletionProvider
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

