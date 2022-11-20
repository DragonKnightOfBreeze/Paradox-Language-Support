package icu.windea.pls.script.exp.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.cwt.*
import icu.windea.pls.script.exp.errors.*
import icu.windea.pls.script.psi.*

class ParadoxScopeLinkDataSourceExpressionNode(
	override val text: String,
	override val rangeInExpression: TextRange,
	val linkConfigs: List<CwtLinkConfig>
) : ParadoxScriptExpressionNode {
	override fun getReference(element: ParadoxScriptExpressionElement): PsiReference {
		return Reference(element, rangeInExpression, linkConfigs)
	}
	
	override fun getUnresolvedError(element: ParadoxScriptExpressionElement): ParadoxScriptExpressionError? {
		if(nodes.isNotEmpty()) return null
		if(text.isEmpty()) return null
		if(text.isParameterAwareExpression()) return null
		//忽略是valueSetValue的情况
		if(linkConfigs.any { it.dataSource?.type == CwtDataTypes.Value }) return null
		val dataSources = linkConfigs.mapNotNull { it.dataSource }.joinToString()
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
				val resolved = CwtConfigHandler.resolveScriptExpression(element, rangeInElement, dataSource, linkConfig)
					?: return@firstNotNullOfOrNull null
				resolved
			}
		}
		
		override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
			val element = element
			return linkConfigs.mapNotNull { linkConfig ->
				val dataSource = linkConfig.dataSource
					?: return@mapNotNull null
				val resolved = CwtConfigHandler.resolveScriptExpression(element, rangeInElement, dataSource, linkConfig)
					?: return@mapNotNull null
				PsiElementResolveResult(resolved)
			}.toTypedArray()
		}
		
		override fun resolveTextAttributesKey(): TextAttributesKey? {
			return super.resolveTextAttributesKey()
		}
	}
}
