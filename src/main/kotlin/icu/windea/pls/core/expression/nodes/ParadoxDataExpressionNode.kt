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
import kotlin.collections.mapNotNullTo

class ParadoxDataExpressionNode (
	override val text: String,
	override val rangeInExpression: TextRange,
	val linkConfigs: List<CwtLinkConfig>
) : ParadoxScriptExpressionNode {
	override fun getAttributesKeyExpression(element: ParadoxScriptExpressionElement, config: CwtDataConfig<*>): CwtDataExpression? {
		if(text.isParameterAwareExpression()) return null
		return linkConfigs.firstNotNullOfOrNull { linkConfig ->
			val dataSource = linkConfig.dataSource
				?: return@firstNotNullOfOrNull null
			CwtConfigHandler.resolveScriptExpression(element, rangeInExpression, dataSource, linkConfig, linkConfig.info.configGroup, exact = false)
				?: return@firstNotNullOfOrNull null
			dataSource
		} ?: linkConfigs.firstOrNull()?.dataSource
	}
	
	override fun getReference(element: ParadoxScriptExpressionElement): Reference? {
		if(text.isParameterAwareExpression()) return null
		return Reference(element, rangeInExpression, linkConfigs)
	}
	
	override fun getUnresolvedError(element: ParadoxScriptExpressionElement): ParadoxExpressionError? {
		if(nodes.isNotEmpty()) return null
		if(text.isEmpty()) return null
		if(text.isParameterAwareExpression()) return null
		//忽略是valueSetValue的情况
		if(linkConfigs.any { it.dataSource?.type == CwtDataTypes.Value }) return null
		val dataSources = linkConfigs.mapNotNullTo(mutableSetOf()) { it.dataSource }.joinToString()
		//排除可解析的情况
		if(getReference(element).canResolve()) return null
		return ParadoxUnresolvedScopeLinkDataSourceExpressionError(rangeInExpression, PlsBundle.message("script.expression.unresolvedData", text, dataSources))
	}
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, linkConfigs: List<CwtLinkConfig>): ParadoxDataExpressionNode {
			//text may contain parameters
			return ParadoxDataExpressionNode(text, textRange, linkConfigs)
		}
	}
	
	class Reference(
		element: ParadoxScriptExpressionElement,
		rangeInElement: TextRange,
		private val linkConfigs: List<CwtLinkConfig>
	) : PsiPolyVariantReferenceBase<ParadoxScriptExpressionElement>(element, rangeInElement), SmartPsiReference {
		override fun handleElementRename(newElementName: String): ParadoxScriptExpressionElement {
			return element.setValue(rangeInElement.replace(element.value, newElementName))
		}
		
		override fun resolve(): PsiElement? {
			return resolve(true)
		}
		
		override fun resolve(exact: Boolean): PsiElement? {
			val element = element
			return linkConfigs.firstNotNullOfOrNull { linkConfig ->
				val dataSource = linkConfig.dataSource
					?: return@firstNotNullOfOrNull null
				val resolved = CwtConfigHandler.resolveScriptExpression(element, rangeInElement, dataSource, linkConfig, linkConfig.info.configGroup, exact = exact)
					?: return@firstNotNullOfOrNull null
				resolved
			}
		}
		
		override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
			val element = element
			return linkConfigs.flatMap { linkConfig ->
				val dataSource = linkConfig.dataSource
					?: return@flatMap emptyList()
				val resolved = CwtConfigHandler.multiResolveScriptExpression(element, rangeInElement, dataSource, linkConfig, linkConfig.info.configGroup)
				resolved
			}.mapToArray { PsiElementResolveResult(it) }
		}
	}
}
