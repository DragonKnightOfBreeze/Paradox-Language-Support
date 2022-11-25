package icu.windea.pls.script.expression.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.*
import icu.windea.pls.script.psi.*

class ParadoxScriptScopeLinkPrefixReference(
	element: ParadoxScriptExpressionElement,
	rangeInElement: TextRange,
	private val resolved: List<PsiElement>?
) : PsiPolyVariantReferenceBase<ParadoxScriptExpressionElement>(element, rangeInElement) {
	override fun handleElementRename(newElementName: String): ParadoxScriptExpressionElement {
		throw IncorrectOperationException() //不允许重命名
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
		return resolved?.mapToArray { PsiElementResolveResult(it) } ?: ResolveResult.EMPTY_ARRAY
	}
}

class ParadoxScriptScopeFieldDataSourceReference(
	element: ParadoxScriptExpressionElement,
	rangeInElement: TextRange,
	private val linkConfigs: List<CwtLinkConfig>
) : PsiPolyVariantReferenceBase<ParadoxScriptExpressionElement>(element, rangeInElement) {
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
	
	override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
		val element = element
		return linkConfigs.mapNotNull { linkConfig ->
			val dataSource = linkConfig.dataSource
				?: return@mapNotNull null
			val resolved = CwtConfigHandler.resolveScriptExpression(element, rangeInElement, dataSource, linkConfig, linkConfig.info.configGroup)
				?: return@mapNotNull null
			ParadoxScriptScopeFieldDataSourceResolveResult(resolved, true, dataSource)
		}.toTypedArray()
	}
}

class ParadoxScriptScopeFieldDataSourceResolveResult(
	element: PsiElement,
	validResult: Boolean = true,
	val expression: CwtValueExpression
) : PsiElementResolveResult(element, validResult)
