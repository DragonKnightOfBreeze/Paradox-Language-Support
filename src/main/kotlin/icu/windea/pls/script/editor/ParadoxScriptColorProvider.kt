package icu.windea.pls.script.editor

import com.intellij.openapi.editor.*
import com.intellij.psi.*
import icu.windea.pls.lang.support.*
import java.awt.*

class ParadoxScriptColorProvider : ElementColorProvider {
	//需要将lineMaker绑定到叶子节点上
	
	override fun getColorFrom(element: PsiElement): Color? {
		ParadoxColorSupport.EP_NAME.extensions.forEach { 
			val targetElement = it.getElementFromToken(element)
			if(targetElement != null) return it.getColor(targetElement)
		}
		return null
	}
	
	override fun setColorTo(element: PsiElement, color: Color) {
		ParadoxColorSupport.EP_NAME.extensions.forEach {
			val targetElement = it.getElementFromToken(element)
			if(targetElement != null) it.setColor(targetElement, color)
		}
	}
}
