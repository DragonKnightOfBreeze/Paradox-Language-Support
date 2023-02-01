package icu.windea.pls.config.core.component

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import icu.windea.pls.script.psi.*
import icu.windea.pls.config.core.config.*

/**
 * 这个扩展点用于在从[ParadoxScriptMemberElement]解析得到相对于向上找到的第一个定义的[ParadoxElementPath]时，
 * 将当前元素替换成另一个元素，以便为某些脚本片段提供更加完善的语言支持。
 * 
 * @see icu.windea.pls.config.core.ParadoxElementPathHandler.linkElement
 */
interface ParadoxElementLinker {
    companion object{
        @JvmStatic val EP_NAME = ExtensionPointName.create<ParadoxElementLinker>("icu.windea.pls.elementLinker")
    }
    
    fun linkElement(element: PsiElement): PsiElement?
}   