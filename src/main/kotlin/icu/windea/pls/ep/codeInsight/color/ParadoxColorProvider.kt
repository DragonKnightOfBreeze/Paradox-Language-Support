package icu.windea.pls.ep.codeInsight.color

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import icu.windea.pls.lang.codeInsight.color.ParadoxColorManager
import icu.windea.pls.lang.codeInsight.color.ParadoxElementColorProvider
import java.awt.Color

/**
 * 用于为各种目标提供颜色的装订线图标，以便查看与修改颜色。
 *
 * 备注：alpha值可以小于0或者大于255（对于浮点数写法则是小于0.0或者大于1.0），表示粒子外溢的光照强度。
 *
 * @see ParadoxElementColorProvider
 * @see ParadoxColorManager
 */
interface ParadoxColorProvider {
    fun getTargetElement(tokenElement: PsiElement): PsiElement?

    fun getColor(element: PsiElement): Color?

    fun setColor(element: PsiElement, color: Color): Boolean

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxColorProvider>("icu.windea.pls.colorProvider")
    }
}
