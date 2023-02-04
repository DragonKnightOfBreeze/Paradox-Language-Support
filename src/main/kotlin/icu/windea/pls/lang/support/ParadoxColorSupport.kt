package icu.windea.pls.lang.support

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import java.awt.*

/**
 * 提供对颜色的支持。（颜色的装订线图标。）
 */
interface ParadoxColorSupport {
    fun getElementFromToken(tokenElement: PsiElement): PsiElement?
    
    fun getColor(element: PsiElement): Color?
    
    fun setColor(element: PsiElement, color: Color)
    
    companion object INSTANCE {
        @JvmStatic val EP_NAME = ExtensionPointName.create<ParadoxColorSupport>("icu.windea.pls.paradoxColorSupport")
    }
}