package icu.windea.pls.config.internal.config

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.util.ui.*
import icu.windea.pls.*
import icu.windea.pls.config.internal.*
import icu.windea.pls.script.psi.*
import java.awt.*
import java.util.*

/**
 * 本地化颜色的内置配置。
 */
class ParadoxColorConfig(
	override val id: String,
	override val description: String,
	val r: Int,
	val g: Int,
	val b: Int,
	val pointer: SmartPsiElementPointer<out PsiElement>
) : IdAware, DescriptionAware, IconAware {
	val color: Color = Color(r, g, b)
	override val icon = ColorIcon(16, color)
	
	val rgbExpression = "{ $r $g $b }"
	val documentation = buildString {
		append(id).append(" = ").append(rgbExpression)
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