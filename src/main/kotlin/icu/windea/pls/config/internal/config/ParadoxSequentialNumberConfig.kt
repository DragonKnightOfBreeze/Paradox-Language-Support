package icu.windea.pls.config.internal.config

import com.intellij.psi.*
import icu.windea.pls.*

class ParadoxSequentialNumberConfig(
	override val id: String,
	override val description: String,
	val placeholderText: String,
	val pointer: SmartPsiElementPointer<out PsiElement>
) : IdAware, DescriptionAware, IconAware {
	val popupText = "$id - $description"
	
	override val icon get() = PlsIcons.localisationSequentialNumberIcon
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxSequentialNumberConfig && id == other.id
	}
	
	override fun hashCode(): Int {
		return id.hashCode()
	}
	
	override fun toString(): String {
		return description
	}
}