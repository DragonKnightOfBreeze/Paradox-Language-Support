package icu.windea.pls.script.editor

import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import java.awt.*

class ParadoxScriptColorProvider : ElementColorProvider {
	//需要将lineMaker绑定到叶子节点上
	
	override fun getColorFrom(element: PsiElement): Color? {
		val targetElement = getTargetElement(element)
		if(targetElement == null) return null
		return ParadoxColorHandler.getColor(targetElement)
	}
	
	override fun setColorTo(element: PsiElement, color: Color) {
		val targetElement = getTargetElement(element)
		if(targetElement == null) return
		return ParadoxColorHandler.setColor(targetElement, color)
	}
	
	private fun getTargetElement(element: PsiElement): ParadoxScriptValue? {
		val elementType = element.elementType
		val targetElement = when {
			elementType == COLOR_TOKEN -> element.parent as? ParadoxScriptColor
			elementType == LEFT_BRACE -> element.parent as? ParadoxScriptBlock
			else -> null
		}
		return targetElement
	}
}
