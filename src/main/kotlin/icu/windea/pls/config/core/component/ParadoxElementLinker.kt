package icu.windea.pls.config.core.component

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import icu.windea.pls.script.psi.*
import icu.windea.pls.config.core.config.*

/**
 * 处理需要内联的脚本内容或者处理脚本片段时，将指定的元素连接到另一个元素，
 * 以便从另一个元素向上查找定义成员和定义，或者获取需要的[ParadoxElementPath]。
 */
interface ParadoxElementLinker {
    companion object{
        @JvmStatic val EP_NAME = ExtensionPointName.create<ParadoxElementLinker>("icu.windea.pls.elementLinker")
    }
    
    fun linkElement(element: PsiElement): PsiElement?
}   