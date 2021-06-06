package icu.windea.pls.localisation.reference

import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

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
		return findLocalisation(name, locale, project,hasDefault = true)
	}

	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		val name = element.propertyReferenceId?.text?: return ResolveResult.EMPTY_ARRAY
		val locale = (element.containingFile as? ParadoxLocalisationFile)?.locale?.paradoxLocale
		val project = element.project
		return findLocalisations(name, locale, project,hasDefault = true).mapToArray {
			PsiElementResolveResult(it)
		}
	}

	override fun getVariants(): Array<out Any> {
		val locale = (element.containingFile as? ParadoxLocalisationFile)?.locale?.paradoxLocale
		val project = element.project
		return findLocalisations(locale, project).mapToArray {
			val name = element.name
			val icon = localisationIcon
			val filePath = it.containingFile.virtualFile.path
			LookupElementBuilder.create(it,name).withIcon(icon).withTailText(filePath,true)
		}
	}
}

