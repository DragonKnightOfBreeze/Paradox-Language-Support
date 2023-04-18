package icu.windea.pls.inject

import net.bytebuddy.agent.*

/**
 * 用于在IDE启动时应用代码注入器。
 *
 * @see CodeInjector
 */
class CodeInjectorService {
    init {
        applyCodeInjectors()
    }
    
    fun applyCodeInjectors() {
        ByteBuddyAgent.install()
        CodeInjector.EP_NAME.extensionList.forEach { 
            it.inject()
        }
    }
}