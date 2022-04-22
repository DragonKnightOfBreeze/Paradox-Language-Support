package icu.windea.pls.config.internal.config

import com.intellij.util.ui.*
import icu.windea.pls.*
import java.awt.*

class ParadoxColorConfig(
	override val id: String,
	override val description: String,
	val colorRgb: Int,
	val colorText: String
) : IdAware, DescriptionAware, IconAware {
	val tailText = " $description"
	val popupText = "$id - $description"
	val color: Color = Color(colorRgb)
	override val icon = ColorIcon(16, color)
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxColorConfig && id == other.id
	}
	
	override fun hashCode(): Int {
		return id.hashCode()
	}
	
	override fun toString(): String {
		return id
	}
}