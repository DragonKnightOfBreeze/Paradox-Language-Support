package icu.windea.pls.model

import com.intellij.util.ui.*
import java.awt.*

class ParadoxColorInfo(
	val name: String,
	val description: String,
	val colorRgb: Int,
	val colorText: String
) {
	val tailText = " $description"
	val popupText = "$name - $description"
	val color: Color = Color(colorRgb)
	val icon = ColorIcon(16, color)
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxColorInfo && name == other.name
	}
	
	override fun hashCode(): Int {
		return name.hashCode()
	}
	
	override fun toString(): String {
		return name
	}
}