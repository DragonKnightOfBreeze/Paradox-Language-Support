package icu.windea.pls.config.internal.config

import com.intellij.psi.*
import com.intellij.util.ui.*
import java.awt.*
import java.util.*

/**
 * 本地化颜色的内置配置。
 */
class ParadoxColorConfig(
	override val id: String,
	override val description: String,
	override val pointer: SmartPsiElementPointer<out PsiElement>,
	val r: Int,
	val g: Int,
	val b: Int
) : InternalConfig {
	val color: Color = Color(r, g, b)
	override val icon = ColorIcon(16, color)
	
	val text = buildString {
		append(id).append(" = { ").append(r).append(" ").append(g).append(" ").append(b).append(" }")
		if(description.isNotEmpty()) append(" (").append(description).append(")")
	}
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxColorConfig && id == other.id && color == other.color
	}
	
	override fun hashCode(): Int {
		return Objects.hash(id, color)
	}
	
	override fun toString(): String {
		return description
	}
}