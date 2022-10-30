package icu.windea.pls.script.expression.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.cwt.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.reference.*

class ParadoxScriptExpressionReference(
	element: ParadoxScriptExpressionElement,
	rangeInElement: TextRange,
	val config: CwtKvConfig<*>,
	val isKey: Boolean
) : PsiReferenceBase<ParadoxScriptExpressionElement>(element, rangeInElement), PsiPolyVariantReference, ParadoxValueSetValueResolvable {
	override fun handleElementRename(newElementName: String): PsiElement {
		//尝试重命名关联的definition、localisation、syncedLocalisation等
		val resolved = resolve()
		when {
			resolved == null -> pass()
			resolved.language == CwtLanguage -> throw IncorrectOperationException() //不允许重命名
			resolved is PsiFile -> resolved.setNameWithoutExtension(newElementName)
			resolved is PsiNamedElement -> resolved.setName(newElementName)
			else -> throw IncorrectOperationException() //不允许重命名
		}
		//重命名引用指向的元素
		return element.setValue(newElementName)
	}
	
	override fun isReferenceTo(element: PsiElement): Boolean {
		//必要的处理
		val resolved = resolve()
		val manager = getElement().manager
		return manager.areElementsEquivalent(resolved, element) || (resolved is ParadoxScriptProperty && manager.areElementsEquivalent(resolved.propertyKey, element))
	}
	
	override fun resolve(): PsiElement? {
		return CwtConfigHandler.resolveScriptExpression(element, rangeInElement, config.expression, config, isKey) //根据对应的expression进行解析
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
		return CwtConfigHandler.multiResolveScriptExpression(element, rangeInElement, config.expression, config, isKey)
			.mapToArray { PsiElementResolveResult(it) } //根据对应的expression进行解析
	}
	
	/**
	 * @see icu.windea.pls.script.codeInsight.completion.ParadoxDefinitionCompletionProvider
	 */
	override fun getVariants(): Array<Any> {
		return super<PsiReferenceBase>.getVariants() //not here
	}
}
