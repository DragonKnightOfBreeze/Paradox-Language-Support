package icu.windea.pls.lang.settings

import com.intellij.util.application
import icu.windea.pls.core.util.CallbackLock
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.listeners.ParadoxDefaultGameDirectoriesListener
import icu.windea.pls.lang.listeners.ParadoxDefaultGameTypeListener
import icu.windea.pls.lang.util.PlsDaemonManager
import icu.windea.pls.model.ParadoxGameType

object PlsSettingsManager {
    fun onDefaultGameTypeChanged(callbackLock: CallbackLock, oldDefaultGameType: ParadoxGameType, newDefaultGameType: ParadoxGameType) {
        if (!callbackLock.check("onDefaultGameTypeChanged")) return

        val messageBus = application.messageBus
        messageBus.syncPublisher(ParadoxDefaultGameTypeListener.TOPIC).onChange(oldDefaultGameType, newDefaultGameType)
    }

    fun onDefaultGameDirectoriesChanged(callbackLock: CallbackLock, oldDefaultGameDirectories: MutableMap<String, String>, newDefaultGameDirectories: MutableMap<String, String>) {
        if (!callbackLock.check("onDefaultGameDirectoriesChanged")) return

        val messageBus = application.messageBus
        messageBus.syncPublisher(ParadoxDefaultGameDirectoriesListener.TOPIC).onChange(oldDefaultGameDirectories, newDefaultGameDirectories)
    }

    @Suppress("UNUSED_PARAMETER")
    fun onPreferredLocaleChanged(callbackLock: CallbackLock, oldPreferredLocale: String?, newPreferredLocale: String?) {
        if (!callbackLock.check("onPreferredLocaleChanged")) return

        ParadoxModificationTrackers.PreferredLocale.incModificationCount()
    }

    // NOTE 如果应用更改时涉及多个相关字段，下面这些回调可能同一回调会被多次调用，不过目前看来问题不大

    fun refreshForFilesByFileNames(callbackLock: CallbackLock, fileNames: MutableSet<String>) {
        if (!callbackLock.check("refreshForFilesByFileNames")) return

        val files = PlsDaemonManager.findFilesByFileNames(fileNames)
        PlsDaemonManager.reparseFiles(files)
    }

    fun refreshForOpenedFiles(callbackLock: CallbackLock) {
        if (!callbackLock.check("refreshForOpenedFiles")) return

        val openedFiles = PlsDaemonManager.findOpenedFiles(onlyParadoxFiles = true)
        PlsDaemonManager.refreshFiles(openedFiles)
    }

    fun refreshForParameterInference(callbackLock: CallbackLock) {
        if (!callbackLock.check("refreshForParameterInference")) return

        ParadoxModificationTrackers.ParameterConfigInference.incModificationCount()

        refreshForOpenedFiles(callbackLock)
    }

    fun refreshForInlineScriptInference(callbackLock: CallbackLock) {
        if (!callbackLock.check("refreshForInlineScriptInference")) return

        ParadoxModificationTrackers.ScriptFile.incModificationCount()
        ParadoxModificationTrackers.InlineScripts.incModificationCount()
        ParadoxModificationTrackers.InlineScriptConfigInference.incModificationCount()

        // 这里只用刷新内联脚本文件
        val openedFiles = PlsDaemonManager.findOpenedFiles(onlyParadoxFiles = true, onlyInlineScriptFiles = true)
        PlsDaemonManager.reparseFiles(openedFiles)
    }

    fun refreshForScopeContextInference(callbackLock: CallbackLock) {
        if (!callbackLock.check("refreshForScopeContextInference")) return

        ParadoxModificationTrackers.DefinitionScopeContextInference.incModificationCount()

        refreshForOpenedFiles(callbackLock)
    }
}
