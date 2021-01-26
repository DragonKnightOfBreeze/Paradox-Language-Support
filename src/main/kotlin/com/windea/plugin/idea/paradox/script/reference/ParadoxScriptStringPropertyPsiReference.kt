package com.windea.plugin.idea.paradox.script.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.psi.*

class ParadoxScriptStringPropertyPsiReference(
	element: ParadoxScriptString,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxScriptString>(element, rangeInElement), PsiPolyVariantReference {
	override fun handleElementRename(newElementName: String): PsiElement {
		return element.setValue(newElementName)
		//TODO 重命名关联的definitionLocalisation
	}
	
	override fun resolve(): PsiElement? {
		val name = element.text.unquote()
		val project = element.project
		//查找的顺序：脚本属性，推断语言区域的本地化属性，所有语言区域的本地化属性
		return findDefinition(name, null, project)
		       ?: findLocalisation(name, inferredParadoxLocale, project, hasDefault = true)
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		val name = element.text.unquote()
		val project = element.project
		//查找的顺序：脚本属性，推断语言区域的本地化属性，所有语言区域的本地化属性
		return findDefinitions(name, null, project)
			.ifEmpty { findLocalisations(name, inferredParadoxLocale, project, hasDefault = true) }
			.mapArray { PsiElementResolveResult(it) }
	}
}
