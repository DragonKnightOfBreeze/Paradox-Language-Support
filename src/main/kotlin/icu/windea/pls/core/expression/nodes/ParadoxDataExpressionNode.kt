package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.*

class ParadoxDataExpressionNode (
	override val text: String,
	override val rangeInExpression: TextRange,
	val linkConfigs: List<CwtLinkConfig>
) : ParadoxExpressionNode {
	override fun getAttributesKeyConfig(element: ParadoxScriptStringExpressionElement): CwtConfig<*>? {
		if(text.isParameterAwareExpression()) return null
		return linkConfigs.find { linkConfig ->
			val dataSource = linkConfig.dataSource ?: return@find false
			CwtConfigHandler.resolveScriptExpression(element, rangeInExpression, linkConfig, linkConfig.expression, linkConfig.info.configGroup, exact = false) != null
		} ?: linkConfigs.firstOrNull()
	}
	
	override fun getReference(element: ParadoxScriptStringExpressionElement): Reference? {
		if(text.isParameterAwareExpression()) return null
		return Reference(element, rangeInExpression, linkConfigs)
	}
	
	override fun getUnresolvedError(element: ParadoxScriptStringExpressionElement): ParadoxExpressionError? {
		if(nodes.isNotEmpty()) return null
		if(text.isEmpty()) return null
		if(text.isParameterAwareExpression()) return null
		//忽略是valueSetValue的情况
		if(linkConfigs.any { it.dataSource?.type == CwtDataType.Value }) return null
		val expect = linkConfigs.mapNotNullTo(mutableSetOf()) { it.expression }.joinToString()
		//排除可解析的情况
		val reference = getReference(element)
		if(reference == null || reference.canResolve()) return null
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
		val linkConfigs: List<CwtLinkConfig>
	) : PsiPolyVariantReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement), PsiNodeReference {
		override fun handleElementRename(newElementName: String): ParadoxScriptStringExpressionElement {
			return element.setValue(rangeInElement.replace(element.value, newElementName))
		}
		
		override fun resolve(): PsiElement? {
			return resolve(true)
		}
		
		override fun resolve(exact: Boolean): PsiElement? {
			val element = element
			return linkConfigs.firstNotNullOfOrNull { linkConfig ->
				val resolved = CwtConfigHandler.resolveScriptExpression(element, rangeInElement, linkConfig, linkConfig.expression, linkConfig.info.configGroup, exact = exact)
					?: return@firstNotNullOfOrNull null
				resolved
			}
		}
		
		override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
			val element = element
			return linkConfigs.flatMap { linkConfig ->
				val resolved = CwtConfigHandler.multiResolveScriptExpression(element, rangeInElement, linkConfig, configExpression = linkConfig.expression, linkConfig.info.configGroup)
				resolved
			}.mapToArray { PsiElementResolveResult(it) }
		}
	}
}

