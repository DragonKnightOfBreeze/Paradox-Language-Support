package icu.windea.pls.lang.settings

import com.intellij.util.application
import icu.windea.pls.core.util.CallbackLock
import icu.windea.pls.ide.analysis.PlsAnalysisManager
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.listeners.ParadoxDefaultGameDirectoriesListener
import icu.windea.pls.lang.listeners.ParadoxDefaultGameTypeListener
import icu.windea.pls.lang.listeners.ParadoxPreferredLocaleListener
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
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

    fun onPreferredLocaleChanged(callbackLock: CallbackLock, oldPreferredLocale: String, newPreferredLocale: String) {
        if (!callbackLock.check("onPreferredLocaleChanged")) return

        ParadoxModificationTrackers.PreferredLocale.incModificationCount()

        val messageBus = application.messageBus
        messageBus.syncPublisher(ParadoxPreferredLocaleListener.TOPIC).onChange(oldPreferredLocale, newPreferredLocale)

        refreshInlayHints(callbackLock)
    }

    fun refreshForFilesByFileNames(callbackLock: CallbackLock, fileNames: MutableSet<String>) {
        if (!callbackLock.check("refreshForFilesByFileNames")) return

        val files = PlsAnalysisManager.findAllFilesByFileNames(fileNames)
        PlsAnalysisManager.reparseFiles(files)
    }

    fun refreshForParameterInference(callbackLock: CallbackLock) {
        if (!callbackLock.check("refreshForParameterInference")) return

        ParadoxModificationTrackers.ParameterConfigInference.incModificationCount()

        refreshFiles(callbackLock)
        refreshInlayHints(callbackLock)
    }

    fun refreshForInlineScriptInference(callbackLock: CallbackLock) {
        if (!callbackLock.check("refreshForInlineScriptInference")) return

        ParadoxModificationTrackers.ScriptFile.incModificationCount()
        ParadoxModificationTrackers.InlineScripts.incModificationCount()
        ParadoxModificationTrackers.InlineScriptConfigInference.incModificationCount()

        // 这里只用刷新内联脚本文件
        val files = PlsAnalysisManager.findAllOpenFiles().filter { ParadoxInlineScriptManager.isInlineScriptFile(it) }
        PlsAnalysisManager.reparseFiles(files)
    }

    fun refreshForScopeContextInference(callbackLock: CallbackLock) {
        if (!callbackLock.check("refreshForScopeContextInference")) return

        ParadoxModificationTrackers.DefinitionScopeContextInference.incModificationCount()

        refreshFiles(callbackLock)
        refreshInlayHints(callbackLock)
    }

    fun refreshFiles(callbackLock: CallbackLock) {
        if (!callbackLock.check("refreshFiles")) return

        PlsAnalysisManager.refreshFiles()
    }

    fun refreshInlayHints(callbackLock: CallbackLock) {
        if (!callbackLock.check("refreshInlayHints")) return

        PlsAnalysisManager.refreshInlayHints()
    }
}
