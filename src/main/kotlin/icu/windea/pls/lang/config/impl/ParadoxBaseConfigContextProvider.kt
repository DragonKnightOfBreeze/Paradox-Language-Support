package icu.windea.pls.lang.config.impl

import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.config.*

/**
 * 用于获取直接的CWT规则上下文。
 */
class ParadoxBaseConfigContextProvider : ParadoxConfigContextProvider {
    override fun getContext(contextElement: PsiElement, file: PsiFile): ParadoxConfigContext? {
        val vFile = file.virtualFile
        if(ParadoxFileManager.isInjectedFile(vFile)) return null //ignored for injected psi
        //TODO
        return null
    }
}
