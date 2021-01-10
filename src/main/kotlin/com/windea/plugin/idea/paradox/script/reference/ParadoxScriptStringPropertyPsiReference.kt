package com.windea.plugin.idea.paradox.script.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.psi.*

class ParadoxScriptStringPropertyPsiReference(
	element: ParadoxScriptString,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxScriptString>(element, rangeInElement), PsiPolyVariantReference {
	private val project = element.project
	
	override fun handleElementRename(newElementName: String): PsiElement {
		return element
	}
	
	override fun resolve(): PsiElement? {
		val name = element.text.unquote()
		//查找的顺序：脚本属性，推断语言区域的本地化属性，所有语言区域的本地化属性
		return findScriptProperty(name, null, project)
		       ?: findLocalisationProperty(name, inferredParadoxLocale, project, defaultToFirst = true)
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		val name = element.text.unquote()
		//查找的顺序：脚本属性，推断语言区域的本地化属性，所有语言区域的本地化属性
		return findScriptProperties(name, null, project)
			.ifEmpty { findLocalisationProperties(name, inferredParadoxLocale, project, defaultToAll = true) }
			.mapArray { PsiElementResolveResult(it) }
	}
	
	
}
