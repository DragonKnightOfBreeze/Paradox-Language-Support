package icu.windea.pls.lang.config.impl

import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于获取脚本参数值中的CWT规则上下文。
 * 
 * 脚本参数值是一个引号括起的字符串，对这个字符串应用自动语言注入，注入为脚本片段，然后获取这个脚本片段中的CWT规则上下文。
 */
class ParadoxScriptSnippetFromParameterValueConfigContextProvider : ParadoxConfigContextProvider {
    override fun getConfigContext(element: ParadoxScriptMemberElement, file: PsiFile): ParadoxConfigContext? {
        val vFile = selectFile(file) ?: return null
        if(!ParadoxFileManager.isInjectedFile(vFile)) return null //limited for injected psi
        
        //TODO
        
        return null
    }
    
    override fun getConfigs(element: ParadoxScriptMemberElement, configContext: ParadoxConfigContext, matchOptions: Int): List<CwtMemberConfig<*>>? {
        return null
    }
}
