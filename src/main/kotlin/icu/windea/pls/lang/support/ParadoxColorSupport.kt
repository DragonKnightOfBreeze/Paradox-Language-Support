package icu.windea.pls.lang.support

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.script.editor.*
import java.awt.*

/**
 * 提供对颜色的支持。（颜色的装订线图标。）
 * @see ParadoxScriptColorProvider
 */
interface ParadoxColorSupport {
    fun getColor(element: PsiElement): Color?
    
    fun setColor(element: PsiElement, color: Color): Boolean
    
    companion object INSTANCE {
        @JvmStatic val EP_NAME = ExtensionPointName.create<ParadoxColorSupport>("icu.windea.pls.paradoxColorSupport")
    
        fun getColor(element: PsiElement): Color? {
            return EP_NAME.extensions.firstNotNullOfOrNull {
                it.getColor(element)
            }
        }
    
        fun setColor(element: PsiElement, color: Color) {
            EP_NAME.extensions.any {
                it.setColor(element, color)
            }
        }
    }
}