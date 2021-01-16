package com.windea.plugin.idea.paradox.localisation.reference

import com.intellij.codeInsight.lookup.*
import com.intellij.model.psi.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.psi.*

@Suppress("UnstableApiUsage")
class ParadoxLocalisationPropertyPsiReference(
	element: ParadoxLocalisationPropertyReference,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationPropertyReference>(element, rangeInElement), PsiPolyVariantReference,PsiCompletableReference {
	private val locale = (element.containingFile as? ParadoxLocalisationFile)?.locale?.paradoxLocale
	private val project = element.project
	
	override fun handleElementRename(newElementName: String): PsiElement {
		return element.setName(newElementName)
		//TODO 重命名关联的definition
	}
	
	override fun resolve(): PsiElement? {
		val name = element.propertyReferenceId?.text?: return null
		return findLocalisation(name, locale, project)
	}

	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		val name = element.propertyReferenceId?.text?: return ResolveResult.EMPTY_ARRAY
		return findLocalisations(name, locale, project).mapArray {
			PsiElementResolveResult(it)
		}
	}

	//注意要传入elementName而非element
	override fun getVariants(): Array<out Any> {
		return findLocalisations(locale, project).mapArray {
			LookupElementBuilder.create(it).withIcon(localisationIcon).withTypeText(it.containingFile.name)
		}
	}
	
	override fun getCompletionVariants(): Collection<LookupElement> {
		return findLocalisations(locale, project).map {
			LookupElementBuilder.create(it).withIcon(localisationIcon).withTypeText(it.containingFile.name)
		}
	}
}

