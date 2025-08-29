package icu.windea.pls.lang.util

import com.intellij.injected.editor.VirtualFileWindow
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFileBase

object PlsVfsManager {
    fun isLightFile(file: VirtualFile): Boolean {
        return file is LightVirtualFileBase
    }

    fun isInjectedFile(file: VirtualFile): Boolean {
        return file is VirtualFileWindow
    }
}
