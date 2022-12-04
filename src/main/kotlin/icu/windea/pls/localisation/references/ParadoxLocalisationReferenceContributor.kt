package icu.windea.pls.localisation.references

import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.*
import icu.windea.pls.localisation.gameTypes.stellaris.references.*
import icu.windea.pls.localisation.psi.*

@Deprecated("UNUSED")
class ParadoxLocalisationReferenceContributor: PsiReferenceContributor() {
	override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
		registrar.registerReferenceProvider(psiElement(ParadoxLocalisationString::class.java), StellarisNameFormatReferenceProvider())
	}
}

