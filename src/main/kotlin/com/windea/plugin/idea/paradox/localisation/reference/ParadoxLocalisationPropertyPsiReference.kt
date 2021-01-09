@file:Suppress("UNCHECKED_CAST", "UnstableApiUsage", "UnstableApiUsage")

package com.windea.plugin.idea.paradox.localisation.reference

import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.psi.*

class ParadoxLocalisationPropertyPsiReference(
	element: ParadoxLocalisationPropertyReference,
	rangeInElement: TextRange
) : PsiReferenceBase<ParadoxLocalisationPropertyReference>(element, rangeInElement), PsiPolyVariantReference {
	private val name = rangeInElement.substring(element.text)
	private val locale = (element.containingFile as? ParadoxLocalisationFile)?.locale?.paradoxLocale
	private val project = element.project
	private val scope = element.resolveScope
	
	//只解析相同语言类型的引用
	
	override fun handleElementRename(newElementName: String): PsiElement {
		return element.setName(newElementName)
	}
	
	override fun resolve(): PsiElement? {
		return findLocalisationProperty(name, locale, project, scope)
	}

	override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
		return findLocalisationProperties(name, locale, project, scope).mapArray {
			PsiElementResolveResult(it)
		}
	}

	//注意要传入elementName而非element
	override fun getVariants(): Array<out Any> {
		return findLocalisationProperties(locale, project, scope).mapArray {
			LookupElementBuilder.create(it).withIcon(it.getIcon(0)).withTypeText(it.containingFile.name).withPsiElement(it)
		}
	}
}

