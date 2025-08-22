package icu.windea.pls.lang.util

import com.intellij.injected.editor.*
import com.intellij.openapi.vfs.*
import com.intellij.testFramework.*

object PlsVfsManager {
    fun isLightFile(file: VirtualFile): Boolean {
        return file is LightVirtualFileBase
    }

    fun isInjectedFile(file: VirtualFile): Boolean {
        return file is VirtualFileWindow
    }
}
