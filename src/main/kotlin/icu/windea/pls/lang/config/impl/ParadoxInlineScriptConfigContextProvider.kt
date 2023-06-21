package icu.windea.pls.lang.config.impl

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.config.*

/**
 * 用于获取内联脚本中的CWT规则上下文。
 */
class ParadoxInlineScriptConfigContextProvider : ParadoxConfigContextProvider {
    override fun getConfigContext(contextElement: PsiElement, file: PsiFile): ParadoxConfigContext? {
        val vFile = selectFile(file) ?: return null
        if(ParadoxFileManager.isInjectedFile(vFile)) return null //ignored for injected psi
        
        //TODO
        
        return null
    }
}