package icu.windea.pls.localisation.reference

import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

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
	
	override fun getVariants(): Array<out Any> {
		val project = element.project
		return findScriptLocalisations(project).mapToArray {
			val name = it.name //与definition.name是相同的，直接使用
			//val name = it.paradoxDefinitionInfo?.name!!
			val icon = scriptLocalisationIcon
			val fileName = it.containingFile.name
			LookupElementBuilder.create(it,name).withIcon(icon).withTypeText(fileName,true)
		}
	}
}
