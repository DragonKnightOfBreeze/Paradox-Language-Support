package icu.windea.pls.script.exp.nodes

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.cwt.*
import icu.windea.pls.script.exp.errors.*
import icu.windea.pls.script.psi.*

class ParadoxScopeLinkDataSourceExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange,
	val linkConfigs: List<CwtLinkConfig>
) : ParadoxScriptExpressionNode {
	override fun getAttributesKeyExpression(element: ParadoxScriptExpressionElement, config: CwtDataConfig<*>): CwtDataExpression? {
		return linkConfigs.firstNotNullOfOrNull { linkConfig ->
			val dataSource = linkConfig.dataSource
				?: return@firstNotNullOfOrNull null
			CwtConfigHandler.resolveScriptExpression(element, rangeInExpression, dataSource, linkConfig, exact = false)
				?: return@firstNotNullOfOrNull null
			dataSource
		} ?: linkConfigs.firstOrNull()?.dataSource
	}
	
	override fun getReference(element: ParadoxScriptExpressionElement) = Reference(element, rangeInExpression, linkConfigs)
	
	override fun getUnresolvedError(element: ParadoxScriptExpressionElement): ParadoxScriptExpressionError? {
		if(nodes.isNotEmpty()) return null
		if(text.isEmpty()) return null
		if(text.isParameterAwareExpression()) return null
		//忽略是valueSetValue的情况
		if(linkConfigs.any { it.dataSource?.type == CwtDataTypes.Value }) return null
		val dataSources = linkConfigs.mapNotNullTo(mutableSetOf()) { it.dataSource }.joinToString()
		//排除可解析的情况
		if(getReference(element).canResolve()) return null
		return ParadoxUnresolvedScopeLinkDataSourceExpressionError(rangeInExpression, PlsBundle.message("script.expression.unresolvedScopeLinkDataSource", text, dataSources))
	}
	
	companion object Resolver {
		fun resolve(text: String, textRange: TextRange, linkConfigs: List<CwtLinkConfig>): ParadoxScopeLinkDataSourceExpressionNode {
			//text may contain parameters
			return ParadoxScopeLinkDataSourceExpressionNode(text, textRange, linkConfigs)
		}
	}
	
	class Reference(
		element: ParadoxScriptExpressionElement,
		rangeInElement: TextRange,
		private val linkConfigs: List<CwtLinkConfig>
	) : PsiPolyVariantReferenceBase<ParadoxScriptExpressionElement>(element, rangeInElement), SmartPsiReference {
		override fun handleElementRename(newElementName: String): ParadoxScriptExpressionElement {
			//尝试重命名关联的definition、localisation、syncedLocalisation等
			val resolved = resolve()
			when {
				resolved == null -> pass()
				resolved.language == CwtLanguage -> throw IncorrectOperationException() //不允许重命名
				resolved is PsiFile -> resolved.setNameWithoutExtension(newElementName)
				resolved is PsiNamedElement -> resolved.setName(newElementName)
				resolved is ParadoxScriptExpressionElement -> resolved.value = newElementName
				else -> throw IncorrectOperationException() //不允许重命名
			}
			//重命名当前元素（仅修改对应范围的文本，认为整个文本没有用引号括起）
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
				val resolved = CwtConfigHandler.resolveScriptExpression(element, rangeInElement, dataSource, linkConfig, exact = exact)
					?: return@firstNotNullOfOrNull null
				resolved
			}
		}
		
		override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
			val element = element
			return linkConfigs.flatMap { linkConfig ->
				val dataSource = linkConfig.dataSource
					?: return@flatMap emptyList()
				val resolved = CwtConfigHandler.multiResolveScriptExpression(element, rangeInElement, dataSource, linkConfig)
				resolved
			}.mapToArray { PsiElementResolveResult(it) }
		}
	}
}
