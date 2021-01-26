package com.windea.plugin.idea.paradox.localisation.reference

import com.intellij.codeInsight.lookup.*
import com.intellij.model.psi.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.psi.*

class ParadoxLocalisationCommandFieldPsiReference(
	element: ParadoxLocalisationCommandField,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationCommandField>(element, rangeInElement) {
	override fun handleElementRename(newElementName: String): PsiElement {
		return element.setName(newElementName)
	}
	
	override fun resolve(): PsiElement? {
		val name = element.commandFieldId?.text?: return null
		val project = element.project
		return findScriptLocalisation(name, project)
	}
	
	//注意要传入elementName而非element
	override fun getVariants(): Array<out Any> {
		val project = element.project
		return findScriptLocalisations(project).mapArray {
			LookupElementBuilder.create(it).withIcon(scriptLocalisationIcon).withTypeText(it.containingFile.name)
		}
	}
}
