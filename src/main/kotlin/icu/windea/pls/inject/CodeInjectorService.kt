package icu.windea.pls.inject

import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service

@Service
class CodeInjectorService : Disposable {
    class Listener : AppLifecycleListener {
        override fun appFrameCreated(commandLineArgs: MutableList<String>) {
            CodeInjectorContext.init()
        }
    }

    override fun dispose() {
        CodeInjectorContext.cleanUp()
    }
}

