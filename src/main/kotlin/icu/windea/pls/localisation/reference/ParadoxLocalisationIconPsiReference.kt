package icu.windea.pls.localisation.reference

import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

class ParadoxLocalisationIconPsiReference(
	element: ParadoxLocalisationIcon,
	rangeInElement: TextRange
): PsiReferenceBase<ParadoxLocalisationIcon>(element,rangeInElement){
	override fun handleElementRename(newElementName: String): PsiElement {
		return element
	}
	
	override fun resolve(): ParadoxScriptProperty? {
		val name = element.name
		val project = element.project
		return findIcon(name,project)
	}
	
	override fun getVariants(): Array<out Any> {
		val project = element.project
		return findIcons(project).mapToArray {
			val name = it.paradoxDefinitionInfo?.name?.let { n-> 
				when{
					n.startsWith("GFX_text_") -> n.substring(9)
					n.startsWith("GFX_") -> n.substring(4)
					else -> n
				}
			}.orEmpty()
			val icon = localisationIconIcon
			val filePath = it.containingFile.virtualFile.path
			LookupElementBuilder.create(it,name).withIcon(icon).withTailText(filePath,true)
		}
	}
}