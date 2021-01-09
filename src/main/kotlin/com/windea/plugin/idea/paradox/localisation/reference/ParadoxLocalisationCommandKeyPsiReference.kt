package com.windea.plugin.idea.paradox.localisation.reference

import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.psi.*

class ParadoxLocalisationCommandKeyPsiReference(
	element: ParadoxLocalisationCommandKey,
	rangeInElement: TextRange
): PsiReferenceBase<ParadoxLocalisationCommandKey>(element,rangeInElement){
	private val name = element.name
	private val project = element.project
	
	override fun handleElementRename(newElementName: String): PsiElement {
		return element.setName(newElementName)
	}
	
	//从scripted_loc中解析
	
	override fun resolve(): PsiElement? {
		return findScriptProperty(name?: return null,"scripted_loc",project,element.resolveScope)
	}
	
	override fun getVariants(): Array<Any> {
		return findScriptProperties("scripted_loc", project).mapArray {
			val icon = localisationCommandKeyIcon
			val fileName = it.containingFile.name
			LookupElementBuilder.create(it).withIcon(icon).withTypeText(fileName).withPsiElement(it)
		}
	}
}

