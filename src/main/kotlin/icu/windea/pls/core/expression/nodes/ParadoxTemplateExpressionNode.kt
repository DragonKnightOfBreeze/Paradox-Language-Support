package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

class ParadoxTemplateExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange,
	val configExpression: CwtDataExpression?,
	val configGroup: CwtConfigGroup
) : ParadoxExpressionNode {
	override fun getReference(element: ParadoxScriptStringExpressionElement): Reference? {
		if(configExpression == null) return null
		if(text.isParameterizedExpression()) return null
		return Reference(element, rangeInExpression, configExpression, configGroup)
	}
	
	override fun getUnresolvedError(element: ParadoxScriptStringExpressionElement): ParadoxExpressionError? {
		if(configExpression == null) return null
		if(text.isParameterizedExpression()) return null
		//排除可解析的情况
		val reference = getReference(element)
		if(reference == null || reference.canResolve()) return null
		val expect = configExpression
		return ParadoxUnresolvedScopeLinkDataSourceExpressionError(rangeInExpression, PlsBundle.message("script.expression.unresolvedData", text, expect))
	}
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, linkConfigs: List<CwtLinkConfig>): ParadoxDataExpressionNode {
			//text may contain parameters
			return ParadoxDataExpressionNode(text, textRange, linkConfigs)
		}
	}
	
	class Reference(
		element: ParadoxScriptStringExpressionElement,
		rangeInElement: TextRange,
		val configExpression: CwtDataExpression,
		val configGroup: CwtConfigGroup
	) : PsiPolyVariantReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement), PsiNodeReference {
		override fun handleElementRename(newElementName: String): ParadoxScriptStringExpressionElement {
			return element.setValue(rangeInElement.replace(element.value, newElementName))
		}
		
		override fun resolve(): PsiElement? {
			return resolve(true)
		}
		
		override fun resolve(exact: Boolean): PsiElement? {
			val element = element
			return ParadoxConfigHandler.resolveScriptExpression(element, rangeInElement, null, configExpression, configGroup, exact = exact)
		}
		
		override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
			val element = element
			return ParadoxConfigHandler.multiResolveScriptExpression(element, rangeInElement, null, configExpression, configGroup)
				.mapToArray { PsiElementResolveResult(it) }
		}
	}
}