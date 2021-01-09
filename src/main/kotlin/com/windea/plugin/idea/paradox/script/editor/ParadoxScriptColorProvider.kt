package com.windea.plugin.idea.paradox.script.editor

import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.windea.plugin.idea.paradox.script.psi.*
import java.awt.*

class ParadoxScriptColorProvider : ElementColorProvider {
	override fun getColorFrom(element: PsiElement): Color? {
		return when(element){
			is ParadoxScriptColor -> element.color
			else -> null
		}
	}

	override fun setColorTo(element: PsiElement, color: Color) {
		when(element){
			is ParadoxScriptColor -> element.setColor(color)
		}
	}
}
