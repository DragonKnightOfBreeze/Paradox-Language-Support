package com.windea.plugin.idea.paradox.script.reference

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.psi.*
import com.windea.plugin.idea.paradox.settings.*

class ParadoxScriptStringAsPropertyPsiReference(
	element: ParadoxScriptString,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxScriptString>(element, rangeInElement), PsiPolyVariantReference {
	//去除包围的引号
	private val name = element.text.unquote()
	private val project = element.project
	private val state = ParadoxSettingsState.getInstance()
	private val scope = element.resolveScope
	
	//不会随之重命名，因为不能保证引用关系正确
	override fun handleElementRename(newElementName: String): PsiElement {
		return element
	}
	
	override fun resolve(): PsiElement? {
		if(state.resolveStringReferences) {
			//查找的顺序：脚本属性，推断语言区域的本地化属性，所有语言区域的本地化属性
			return findScriptProperty(name, project, scope)
			       ?: findLocalisationPropertyOrFirst(name, inferredParadoxLocale, project, scope)
		}
		return null
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		if(state.resolveStringReferences) {
			//查找的顺序：脚本属性，推断语言区域的本地化属性，所有语言区域的本地化属性
			return findScriptProperties(name, project,scope)
				.ifEmpty { findLocalisationPropertiesOrAll(name, inferredParadoxLocale, project, scope) }
				.mapArray { PsiElementResolveResult(it) }
		}
		return ResolveResult.EMPTY_ARRAY
	}
}
