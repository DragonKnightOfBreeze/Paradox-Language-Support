package icu.windea.pls.config.internal.config

import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.*

class ParadoxLocaleConfig(
	override val id: String,
	override val description: String,
	val languageTag: String,
	val pointer: SmartPsiElementPointer<out PsiElement>
) : IdAware, DescriptionAware, IconAware {
	override val icon get() = PlsIcons.localisationLocaleIcon
	
	val popupText = "$id - $description"
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxLocaleConfig && id == other.id
	}
	
	override fun hashCode(): Int {
		return id.hashCode()
	}
	
	override fun toString(): String {
		return description
	}
}