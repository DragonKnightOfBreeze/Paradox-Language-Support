package icu.windea.pls.inject

import com.intellij.ide.*
import com.intellij.openapi.components.*

class CodeInjectorServiceListener: AppLifecycleListener {
    override fun appFrameCreated(commandLineArgs: MutableList<String>) {
        val codeInjectorService = service<CodeInjectorService>()
        codeInjectorService.init()
    }
}