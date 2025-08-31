package icu.windea.pls.lang.util

import com.intellij.injected.editor.VirtualFileWindow
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFileBase
import com.intellij.util.application
import icu.windea.pls.lang.PlsKeys
import icu.windea.pls.model.ParadoxGameType

object PlsVfsManager {
    fun isLightFile(file: VirtualFile): Boolean {
        return file is LightVirtualFileBase
    }

    fun isInjectedFile(file: VirtualFile): Boolean {
        return file is VirtualFileWindow
    }

    fun isTestDataFile(file: VirtualFile): Boolean {
        if (!application.isUnitTestMode) return false
        val name = file.nameWithoutExtension
        return name.split('_', '.').any { it == "test" }
    }

    fun getInjectedGameTypeForTestDataFile(file: VirtualFile): ParadoxGameType? {
        if (!isTestDataFile(file)) return null
        val name = file.nameWithoutExtension
        val injectedGameType = name.split('_', '.').firstNotNullOfOrNull { ParadoxGameType.resolve(it) }
        file.putUserData(PlsKeys.injectedGameType, injectedGameType)
        return injectedGameType
    }
}
