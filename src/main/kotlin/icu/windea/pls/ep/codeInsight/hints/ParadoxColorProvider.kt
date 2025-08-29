package icu.windea.pls.ep.codeInsight.hints

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import java.awt.Color

/**
 * 用于为脚本文件中的各种目标提供颜色的装订线图标，以便查看与修改颜色。
 *
 * 备注：alpha值可以小于0或者大于255（对于浮点数写法则是小于0.0或者大于1.0），表示粒子外溢的光照强度。
 *
 * @see icu.windea.pls.script.editor.ParadoxScriptColorProvider
 * @see com.intellij.openapi.editor.ElementColorProvider
 * @see com.intellij.ui.ColorLineMarkerProvider
 */
interface ParadoxColorProvider {
    fun getTargetElement(tokenElement: PsiElement): PsiElement?

    fun getColor(element: PsiElement): Color?

    fun setColor(element: PsiElement, color: Color): Boolean

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxColorProvider>("icu.windea.pls.colorProvider")

        fun getColor(element: PsiElement, fromToken: Boolean = false): Color? {
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                val targetElement = if (fromToken) ep.getTargetElement(element) else element
                if (targetElement == null) return@f null
                ep.getColor(targetElement)
            }
        }

        fun setColor(element: PsiElement, color: Color, fromToken: Boolean = false) {
            EP_NAME.extensionList.any f@{ ep ->
                val targetElement = if (fromToken) ep.getTargetElement(element) else element
                if (targetElement == null) return@f false
                ep.setColor(targetElement, color)
            }
        }
    }
}
