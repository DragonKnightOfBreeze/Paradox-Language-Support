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
		//TODO 重命名关联的definition
		return element.setName(newElementName)
	}
	
	override fun resolve(): PsiElement? {
		val name = element.name
		val locale = (element.containingFile as? ParadoxLocalisationFile)?.locale?.paradoxLocale
		val project = element.project
		return findLocalisation(name, locale, project, hasDefault = true)
	}
	
	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		val name = element.name
		val locale = (element.containingFile as? ParadoxLocalisationFile)?.locale?.paradoxLocale
		val project = element.project
		return findLocalisations(name, locale, project, hasDefault = true).mapToArray {
			PsiElementResolveResult(it)
		}
	}
	
	override fun getVariants(): Array<out Any> {
		val project = element.project
		//为了避免这里得到的结果太多，采用关键字查找，这里要去掉作为后缀的dummyIdentifier，并且捕捉异常防止意外
		val keyword = runCatching { element.name.dropLast(dummyIdentifierLength) }.getOrElse { return emptyArray() }
		return findLocalisationsByKeyword(keyword, project).mapToArray {
			val name = it.name
			val icon = localisationIcon
			//val typeText = it.paradoxFileInfo?.path.toStringOrEmpty()
			val typeText = it.containingFile.name
			LookupElementBuilder.create(it, name).withIcon(icon).withTypeText(typeText, true)
		}
	}
}

