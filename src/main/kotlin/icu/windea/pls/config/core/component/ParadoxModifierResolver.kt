package icu.windea.pls.config.core.component

import com.intellij.openapi.extensions.*
import com.intellij.psi.*
import icu.windea.pls.core.psi.*

/**
 * 处理如何解析生成的修饰符，以及获取修饰符的生成源信息。
 * 
 * @see ParadoxModifierElement
 */
interface ParadoxModifierResolver {
    fun resolveModifier(name: String, element: PsiElement): ParadoxModifierElement?
    
    companion object INSTANCE {
        @JvmStatic
        val EP_NAME = ExtensionPointName.create<ParadoxModifierResolver>("icu.windea.pls.paradoxModifierResolver")
        
        fun resolveModifier(name: String, element: PsiElement): ParadoxModifierElement? {
            return EP_NAME.extensions.firstNotNullOfOrNull { it.resolveModifier(name, element) }
        }
    }
}