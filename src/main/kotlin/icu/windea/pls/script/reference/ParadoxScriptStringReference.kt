package icu.windea.pls.script.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.cwt.expression.*
import icu.windea.pls.script.psi.*
import kotlin.text.removeSurrounding

class ParadoxScriptStringReference(
	element: ParadoxScriptString,
	rangeInElement: TextRange
): PsiReferenceBase<ParadoxScriptString>(element,rangeInElement), PsiPolyVariantReference {
	override fun handleElementRename(newElementName: String): PsiElement {
		//尝试重命名关联的definition、localisation、syncedLocalisation等
		val resolved = resolve()
		when {
			resolved == null -> pass()
			!resolved.isWritable -> throw IncorrectOperationException(message("cannotBeRenamed"))
			else -> resolved.setName(newElementName)
		}
		return element.setValue(newElementName)
	}
	
	override fun resolve(): PsiNamedElement? {
		//根据对应的expression进行解析
		//val expression = element.expression?:return null
		//NOTE 由于目前引用支持不完善，如果expression为null时需要进行回调解析引用
		val expression = element.expression?:return fallbackResolve()
		val project = element.project
		return when(expression.type){
			CwtValueExpression.Type.TypeExpression -> {
				val name = element.value
				val typeExpression = expression.value?:return null
				findDefinitionByType(name, typeExpression, project)
			}
			CwtValueExpression.Type.TypeExpressionString -> {
				val (prefix,suffix) = expression.extraValue.castOrNull<Pair<String, String>>()?:return null
				val name = element.value.removeSurrounding(prefix,suffix)
				val typeExpression = expression.value?: return null
				findDefinitionByType(name, typeExpression, project)
			}
			CwtValueExpression.Type.Localisation -> {
				val name = element.value
				findLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
			}
			CwtValueExpression.Type.SyncedLocalisation -> {
				val name = element.value
				findSyncedLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
			}
			CwtValueExpression.Type.AliasMatchLeftExpression -> fallbackResolve() //TODO
			else -> null //TODO
		}
	}
	
	private fun fallbackResolve():PsiNamedElement?{
		val name = element.value
		val project = element.project
		return findDefinition(name,null,project)
			?: findLocalisation(name, inferParadoxLocale(),project, hasDefault = true)
			?: findSyncedLocalisation(name, inferParadoxLocale(),project,hasDefault = true)
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
		//根据对应的expression进行解析
		//val expression = element.expression?:return emptyArray()
		//NOTE 由于目前引用支持不完善，如果expression为null时需要进行回调解析引用
		val expression = element.expression?:return fallbackMultiResolve(incompleteCode)
		val project = element.project
		return when(expression.type){
			CwtValueExpression.Type.TypeExpression -> {
				val name = element.value
				val typeExpression = expression.value?:return emptyArray()
				findDefinitionsByType(name, typeExpression, project)
			}
			CwtValueExpression.Type.TypeExpressionString -> {
				val (prefix,suffix) = expression.extraValue.castOrNull<Pair<String, String>>()?:return emptyArray()
				val name = element.value.removeSurrounding(prefix,suffix)
				val typeExpression = expression.value?: return emptyArray()
				findDefinitionsByType(name, typeExpression, project)
			}
			CwtValueExpression.Type.Localisation -> {
				val name = element.value
				findLocalisations(name, inferParadoxLocale(), project, hasDefault = true)
			}
			CwtValueExpression.Type.SyncedLocalisation -> {
				val name = element.value
				findSyncedLocalisations(name, inferParadoxLocale(), project, hasDefault = true)
			}
			CwtValueExpression.Type.AliasMatchLeftExpression -> return fallbackMultiResolve(incompleteCode) //TODO
			else -> return emptyArray() //TODO
		}.mapToArray { PsiElementResolveResult(it) }
	}
	
	private fun fallbackMultiResolve(incompleteCode: Boolean):Array<ResolveResult>{
		val name = element.value
		val project = element.project
		return findDefinitions(name,null,project)
			.ifEmpty{ findLocalisations(name, inferParadoxLocale(),project, hasDefault = true)}
			.ifEmpty{ findSyncedLocalisations(name, inferParadoxLocale(),project,hasDefault = true)}
			.mapToArray { PsiElementResolveResult(it) }
	}
	
	//代码提示功能由ParadoxScriptCompletionContributor统一实现
}