package icu.windea.pls.localisation.reference

import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.definition.*
import icu.windea.pls.core.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationColorReference(
	element: ParadoxLocalisationColorfulText,
	rangeInElement: TextRange
): PsiReferenceBase<ParadoxLocalisationColorfulText>(element, rangeInElement){
	override fun handleElementRename(newElementName: String): PsiElement {
		throw IncorrectOperationException() //不允许重命名
	}
	
	override fun resolve(): PsiElement? {
		return element.colorConfig?.pointer?.element
	}
	
	///**
	// * @see icu.windea.pls.localisation.codeInsight.completion.ParadoxColorCompletionProvider
	// */
	@Suppress("RedundantOverride")
	override fun getVariants(): Array<out Any> {
		val file = element.containingFile
		val project = element.project
		val gameType = ParadoxSelectorUtils.selectGameType(file) ?: return LookupElement.EMPTY_ARRAY
		val colorConfigs = DefinitionConfigHandler.getTextColorConfigs(gameType, project, file)
		
		return super.getVariants() //not here
	}
}