package icu.windea.pls.script.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * @see icu.windea.pls.script.codeInsight.completion.ParadoxDefinitionCompletionProvider
 */
class ParadoxScriptExpressionPsiReference(
	element: ParadoxScriptExpressionElement,
	rangeInElement: TextRange,
	val config: CwtDataConfig<*>,
	val isKey: Boolean
) : PsiPolyVariantReferenceBase<ParadoxScriptExpressionElement>(element, rangeInElement) {
	val project by lazy { config.info.configGroup.project }
	//val project by lazy { element.project }
	
	override fun handleElementRename(newElementName: String): PsiElement {
		val element = element
		return when {
			element is ParadoxScriptStringExpressionElement -> element.setValue(rangeInElement.replace(element.value, newElementName))
			element is ParadoxScriptInt -> element.setValue(rangeInElement.replace(element.value, newElementName))
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
	
	//缓存解析结果以优化性能
	
	override fun resolve(): PsiElement? {
		return ResolveCache.getInstance(project).resolveWithCaching(this, Resolver, false, false)
	}
	
	private fun doResolve(): PsiElement? {
		//根据对应的expression进行解析
		return ParadoxConfigHandler.resolveScriptExpression(element, rangeInElement, config, config.expression, config.info.configGroup, isKey)
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		return ResolveCache.getInstance(project).resolveWithCaching(this, MultiResolver, false, false)
	}
	
	private fun doMultiResolve(): Array<out ResolveResult> {
		//根据对应的expression进行解析
		return ParadoxConfigHandler.multiResolveScriptExpression(element, rangeInElement, config, config.expression, config.info.configGroup, isKey)
			.mapToArray { PsiElementResolveResult(it) }
	}
	
	private object Resolver: ResolveCache.AbstractResolver<ParadoxScriptExpressionPsiReference, PsiElement> {
		override fun resolve(ref: ParadoxScriptExpressionPsiReference, incompleteCode: Boolean): PsiElement? {
			return ref.doResolve()
		}
	}
	
	private object MultiResolver: ResolveCache.PolyVariantResolver<ParadoxScriptExpressionPsiReference> {
		override fun resolve(ref: ParadoxScriptExpressionPsiReference, incompleteCode: Boolean): Array<out ResolveResult> {
			return ref.doMultiResolve()
		}
	}
}

