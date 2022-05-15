package icu.windea.pls.localisation.navigation

import com.intellij.navigation.*
import com.intellij.psi.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationPresentationProvider : ItemPresentationProvider<NavigatablePsiElement> {
	override fun getPresentation(item: NavigatablePsiElement): ItemPresentation? {
		return when {
			item is ParadoxLocalisationFile -> ParadoxLocalisationFilePresentation(item)
			item is ParadoxLocalisationProperty -> ParadoxLocalisationPropertyPresentation(item)
			else -> null
		}
	}
}