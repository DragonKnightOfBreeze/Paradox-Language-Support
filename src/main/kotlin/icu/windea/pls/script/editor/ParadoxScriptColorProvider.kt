package icu.windea.pls.script.editor

import com.intellij.openapi.editor.*
import com.intellij.psi.*
import icu.windea.pls.lang.color.*
import java.awt.*

class ParadoxScriptColorProvider : ElementColorProvider {
	//不要将lineMarker绑定到叶子节点上，否则保持打开颜色设置对话框时，第一次之后的替换不会生效（因为目标PSI元素已经非法了）
	
	override fun getColorFrom(element: PsiElement): Color? {
		return ParadoxColorSupport.getColor(element)
	}
	
	override fun setColorTo(element: PsiElement, color: Color) {
		ParadoxColorSupport.setColor(element, color)
	}
}
