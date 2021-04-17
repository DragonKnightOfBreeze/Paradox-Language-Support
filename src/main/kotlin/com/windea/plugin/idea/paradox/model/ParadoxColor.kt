package com.windea.plugin.idea.paradox.model

import com.intellij.util.ui.*
import java.awt.*

class ParadoxColor(data:Map<String,Any>){
	val name: String by data
	val description: String by data
	val colorRgb:Int by data
	val colorText:String by data

	val popupText = "$name - $description"
	val color: Color = Color(colorRgb)
	val icon = ColorIcon(16, color)
	val gutterIcon = ColorIcon(12, color)
	
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxColor && name == other.name
	}
	
	override fun hashCode(): Int {
		return name.hashCode()
	}
	
	override fun toString(): String {
		return name
	}
}