package icu.windea.pls.localisation.reference

import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationCommandFieldReference(
	element: ParadoxLocalisationCommandField,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationCommandField>(element, rangeInElement) {
	override fun handleElementRename(newElementName: String): PsiElement {
		//TODO
		return element.setName(newElementName)
	}
	
	override fun resolve(): PsiElement? {
		//查找类型为scripted_loc的definition
		val name = element.name
		val project = element.project
		return findDefinitionByType(name, "scripted_loc", project)
	}
	
	override fun getVariants(): Array<out Any> {
		//为了避免这里得到的结果太多，采用关键字查找
		val keyword = element.keyword
		val project = element.project
		//查找类型为scripted_loc的definition
		return findDefinitionsByKeywordByType(keyword,"scripted_loc", project).mapToArray {
			val name = it.definitionInfo?.name.orEmpty() //不应该为空
			val icon = PlsIcons.localisationCommandFieldIcon
			val typeText = it.containingFile.name
			LookupElementBuilder.create(it, name).withIcon(icon).withTypeText(typeText, true)
		}
	}
}
