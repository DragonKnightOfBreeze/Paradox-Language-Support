package icu.windea.pls.localisation.reference

import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationIconPsiReference(
	element: ParadoxLocalisationIcon,
	rangeInElement: TextRange
): PsiReferenceBase<ParadoxLocalisationIcon>(element,rangeInElement){
	override fun handleElementRename(newElementName: String): PsiElement {
		return element
	}
	
	override fun resolve(): PsiElement? {
		val name = element.name
		val project = element.project
		return findIcon(name,project)
	}
	
	//注意要传入elementName而非element
	override fun getVariants(): Array<out Any> {
		val project = element.project
		return findIcons(project).mapToArray {
			LookupElementBuilder.create(it).withIcon(localisationIconIcon).withTypeText(it.containingFile.name)
		}
	}
}