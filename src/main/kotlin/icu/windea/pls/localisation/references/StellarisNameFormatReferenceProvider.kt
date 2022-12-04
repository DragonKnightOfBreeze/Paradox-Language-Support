package icu.windea.pls.localisation.references

import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.localisation.psi.*

@Deprecated("UNUSED")
class StellarisNameFormatReferenceProvider: PsiReferenceProvider(){
	override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
		if(element !is ParadoxLocalisationString) return PsiReference.EMPTY_ARRAY
		return PsiReference.EMPTY_ARRAY
	}
	
	override fun acceptsTarget(target: PsiElement): Boolean {
		return ParadoxSelectorUtils.selectGameType(target) == ParadoxGameType.Stellaris
	}
}