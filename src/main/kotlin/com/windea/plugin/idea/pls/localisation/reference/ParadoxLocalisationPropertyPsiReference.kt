package com.windea.plugin.idea.pls.localisation.reference

import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.windea.plugin.idea.pls.*
import com.windea.plugin.idea.pls.localisation.psi.*

class ParadoxLocalisationPropertyPsiReference(
	element: ParadoxLocalisationPropertyReference,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationPropertyReference>(element, rangeInElement), PsiPolyVariantReference {
	override fun handleElementRename(newElementName: String): PsiElement {
		return element.setName(newElementName)
		//TODO 重命名关联的definition
	}
	
	override fun resolve(): PsiElement? {
		val name = element.propertyReferenceId?.text?: return null
		val locale = (element.containingFile as? ParadoxLocalisationFile)?.locale?.paradoxLocale
		val project = element.project
		return findLocalisation(name, locale, project)
	}

	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		val name = element.propertyReferenceId?.text?: return ResolveResult.EMPTY_ARRAY
		val locale = (element.containingFile as? ParadoxLocalisationFile)?.locale?.paradoxLocale
		val project = element.project
		return findLocalisations(name, locale, project).mapArray {
			PsiElementResolveResult(it)
		}
	}

	//注意要传入elementName而非element
	override fun getVariants(): Array<out Any> {
		val locale = (element.containingFile as? ParadoxLocalisationFile)?.locale?.paradoxLocale
		val project = element.project
		return findLocalisations(locale, project).mapArray {
			LookupElementBuilder.create(it).withIcon(localisationIcon).withTypeText(it.containingFile.name)
		}
	}
}

