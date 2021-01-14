package com.windea.plugin.idea.paradox.localisation.reference

import com.intellij.codeInsight.lookup.*
import com.intellij.model.psi.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.psi.*

@Suppress("UnstableApiUsage")
class ParadoxLocalisationCommandFieldPsiReference(
	element: ParadoxLocalisationCommandField,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationCommandField>(element, rangeInElement),PsiCompletableReference {
	private val project = element.project
	
	override fun handleElementRename(newElementName: String): PsiElement {
		return element.setName(newElementName)
	}
	
	//TODO 不完全 - 有些localisationCommandField并没有对应的scripted_loc，如GetName
	
	override fun resolve(): PsiElement? {
		val name = element.commandFieldId?.text?: return null
		return findScriptLoc(name , project)
	}
	
	//注意要传入elementName而非element
	override fun getVariants(): Array<out Any> {
		val icon = scriptedLocIcon
		return findScriptLocs(project).mapArray {
			val fileName = it.containingFile.name
			LookupElementBuilder.create(it).withIcon(icon).withTypeText(fileName).withPsiElement(it)
		}
	}
	
	override fun getCompletionVariants(): Collection<LookupElement> {
		val icon = scriptedLocIcon
		return findScriptLocs(project).map {
			val fileName = it.containingFile.name
			LookupElementBuilder.create(it).withIcon(icon).withTypeText(fileName).withPsiElement(it)
		}
	}
}
