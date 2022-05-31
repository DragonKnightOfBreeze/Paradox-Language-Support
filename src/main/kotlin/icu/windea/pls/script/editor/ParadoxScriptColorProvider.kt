package icu.windea.pls.script.editor

import com.intellij.openapi.editor.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.ParadoxScriptElementTypes.*
import java.awt.*

class ParadoxScriptColorProvider : ElementColorProvider {
	//需要将lineMaker绑定到叶子节点上
	
	override fun getColorFrom(element: PsiElement): Color? {
		return when {
			element.firstChild != null -> null
			element is PsiWhiteSpace -> null
			element.language != ParadoxScriptLanguage -> null
			else -> {
				val elementType = element.elementType
				when {
					elementType == COLOR_TOKEN -> element.parent?.castOrNull<ParadoxScriptColor>()?.color
					elementType == LEFT_BRACE -> element.parent?.castOrNull<ParadoxScriptBlock>()?.color
					else -> null
				}
			}
		}
	}
	
	override fun setColorTo(element: PsiElement, color: Color) {
		when {
			element.firstChild != null -> pass()
			element is PsiWhiteSpace -> pass()
			element.language != ParadoxScriptLanguage -> pass()
			else -> {
				val elementType = element.elementType
				when {
					elementType == COLOR_TOKEN -> element.parent?.castOrNull<ParadoxScriptColor>()?.setColor(color)
					elementType == LEFT_BRACE -> element.parent?.castOrNull<ParadoxScriptBlock>()?.setColor(color)
					else -> pass()
				}
			}
		}
	}
}
