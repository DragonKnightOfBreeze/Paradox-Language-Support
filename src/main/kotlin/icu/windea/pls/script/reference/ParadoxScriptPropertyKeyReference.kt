package icu.windea.pls.script.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.cwt.expression.*
import icu.windea.pls.script.psi.*

class ParadoxScriptPropertyKeyReference(
	element:ParadoxScriptPropertyKey,
	rangeInElement:TextRange
):PsiReferenceBase<ParadoxScriptPropertyKey>(element,rangeInElement),PsiPolyVariantReference {
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
		val expression = element.expression?:return null
		val project = element.project
		return when(expression.type){
			CwtKeyExpression.Type.TypeExpression -> {
				val name = element.value
				val typeExpression = expression.value?:return null
				findDefinitionByType(name,typeExpression,project)
			}
			CwtKeyExpression.Type.TypeExpressionString -> {
				val (prefix,suffix) = expression.extraValue.castOrNull<Pair<String,String>>()?:return null
				val name = element.value.removeSurrounding(prefix,suffix)
				val typeExpression = expression.value?: return null
				findDefinitionByType(name,typeExpression,project)
			}
			CwtKeyExpression.Type.Localisation -> {
				val name = element.value
				findLocalisation(name, inferParadoxLocale(),project,hasDefault = true)
			}
			CwtKeyExpression.Type.SyncedLocalisation -> {
				val name = element.value
				findSyncedLocalisation(name, inferParadoxLocale(),project,hasDefault = true)
			}
			else -> null //TODO
		}
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
		//根据对应的expression进行解析
		val expression = element.expression?:return emptyArray()
		val project = element.project
		return when(expression.type){
			CwtKeyExpression.Type.TypeExpression -> {
				val name = element.value
				val typeExpression = expression.value?:return emptyArray()
				findDefinitionsByType(name,typeExpression,project)
			}
			CwtKeyExpression.Type.TypeExpressionString -> {
				val (prefix,suffix) = expression.extraValue.castOrNull<Pair<String,String>>()?:return emptyArray()
				val name = element.value.removeSurrounding(prefix,suffix)
				val typeExpression = expression.value?: return emptyArray()
				findDefinitionsByType(name,typeExpression,project)
			}
			CwtKeyExpression.Type.Localisation -> {
				val name = element.value
				findLocalisations(name, inferParadoxLocale(),project,hasDefault = true)
			}
			CwtKeyExpression.Type.SyncedLocalisation -> {
				val name = element.value
				findSyncedLocalisations(name, inferParadoxLocale(),project,hasDefault = true)
			}
			else -> return emptyArray() //TODO
		}.mapToArray { PsiElementResolveResult(it) }
	}
	
	//代码提示功能由ParadoxScriptCompletionContributor统一实现
}