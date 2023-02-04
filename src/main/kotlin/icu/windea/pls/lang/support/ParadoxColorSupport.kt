package icu.windea.pls.lang.support

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import java.awt.*

/**
 * 提供对颜色的支持。（颜色的装订线图标。）
 */
interface ParadoxColorSupport {
    fun supports(element: PsiElement): Boolean
    
    fun getColor(element: PsiElement): Color?
    
    fun setColor(element: PsiElement, color: Color)
    
    companion object INSTANCE{
        @JvmStatic val EP_NAME = ExtensionPointName.create<ParadoxColorSupport>("icu.windea.pls.paradoxColorSupport")
        
        fun getColor(element: PsiElement): Color? {
            EP_NAME.extensions.forEach { 
                if(it.supports(element)) return it.getColor(element)
            }
            return null
        }
        
        fun setColor(element: PsiElement, color: Color) {
            EP_NAME.extensions.forEach {
                if(it.supports(element)) return it.setColor(element, color)
            }
        }
    }
}