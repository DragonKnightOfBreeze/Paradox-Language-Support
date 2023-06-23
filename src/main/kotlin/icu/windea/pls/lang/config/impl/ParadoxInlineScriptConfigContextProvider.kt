package icu.windea.pls.lang.config.impl

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于获取内联脚本中的CWT规则上下文。
 */
class ParadoxInlineScriptConfigContextProvider : ParadoxConfigContextProvider {
    override fun getConfigContext(element: ParadoxScriptMemberElement, file: PsiFile): ParadoxConfigContext? {
        val vFile = selectFile(file) ?: return null
        if(ParadoxFileManager.isInjectedFile(vFile)) return null //ignored for injected psi
        
        //TODO
        
        return null
    }
    
    override fun getConfigs(element: ParadoxScriptMemberElement, configContext: ParadoxConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        return null
    }
}