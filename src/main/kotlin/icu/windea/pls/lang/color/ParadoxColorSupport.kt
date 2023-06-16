package icu.windea.pls.lang.color

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.script.editor.*
import java.awt.*

/**
 * 提供对颜色的支持。（显示颜色装订线图标）
 *
 * 备注：alpha值可以小于0或者大于255（对于浮点数写法则是小于0.0或者大于1.0），表示粒子外溢的光照强度。
 *
 * @see ParadoxScriptColorProvider
 */
interface ParadoxColorSupport {
    fun getColor(element: PsiElement): Color?
    
    fun setColor(element: PsiElement, color: Color): Boolean
    
    companion object INSTANCE {
        val EP_NAME = ExtensionPointName.create<ParadoxColorSupport>("icu.windea.pls.colorSupport")
        
        fun getColor(element: PsiElement): Color? {
            return EP_NAME.extensionList.firstNotNullOfOrNull { ep ->
                ep.getColor(element)
            }
        }
        
        fun setColor(element: PsiElement, color: Color) {
            EP_NAME.extensionList.any {
                it.setColor(element, color)
            }
        }
    }
}