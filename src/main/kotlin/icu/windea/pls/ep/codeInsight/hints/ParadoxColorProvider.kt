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
    }
}
