package icu.windea.pls.config.internal.config

import com.intellij.psi.*
import icu.windea.pls.*

/**
 * 本地化语言区域的内置配置。
 */
class ParadoxLocaleConfig(
	override val id: String,
	override val description: String,
	val languageTag: String,
	override val pointer: SmartPsiElementPointer<out PsiElement>
) : InternalConfig{
	override val icon get() = PlsIcons.LocalisationLocale
	
	val text = buildString {
		append(id)
		if(description.isNotEmpty()) append(" (").append(description).append(")")
	}
	
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