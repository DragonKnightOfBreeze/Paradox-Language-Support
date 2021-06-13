package icu.windea.pls.script.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.script.psi.*

class ParadoxScriptStringPropertyPsiReference(
	element: ParadoxScriptString,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxScriptString>(element, rangeInElement), PsiPolyVariantReference {
	override fun handleElementRename(newElementName: String): PsiElement {
		//TODO 重命名关联的definitionLocalisation
		return element.setValue(newElementName)
	}
	
	override fun resolve(): PsiElement? {
		val name = element.text.unquote()
		val project = element.project
		//查找的顺序：definition，或者localisation，或者localisation_synced（如果存在）
		return findDefinition(name, null, project)
			?: findLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
			?: findSyncedLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		val name = element.text.unquote()
		val project = element.project
		//查找的顺序：definition，或者localisation，或者localisation_synced（如果存在）
		return findDefinitions(name, null, project)
			.ifEmpty { findLocalisations(name, inferParadoxLocale(), project, hasDefault = true) }
			.ifEmpty { findSyncedLocalisations(name, inferParadoxLocale(), project, hasDefault = true) }
			.mapToArray { PsiElementResolveResult(it) }
	}
}
