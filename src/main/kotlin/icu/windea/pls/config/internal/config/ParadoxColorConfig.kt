package icu.windea.pls.config.internal.config

import com.intellij.psi.*
import com.intellij.util.ui.*
import icu.windea.pls.*
import java.awt.*

class ParadoxColorConfig(
	override val id: String,
	override val description: String,
	val colorRgb: String,
	val pointer: SmartPsiElementPointer<out PsiElement>
) : IdAware, DescriptionAware, IconAware {
	val popupText = "$id - $description"
	val color: Color = Color(colorRgb.drop(1).toInt(16))
	override val icon = ColorIcon(16, color)
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxColorConfig && id == other.id
	}
	
	override fun hashCode(): Int {
		return id.hashCode()
	}
	
	override fun toString(): String {
		return description
	}
}