package icu.windea.pls.lang.analysis

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.paths.ParadoxPath

@Suppress("unused")
object ParadoxAnalysisInjector {
    private val dataService get() = ParadoxAnalysisDataService.getInstance()

    // region Get Methods

    fun getInjectedRootInfo(rootFile: VirtualFile): ParadoxRootInfo? {
        return with(dataService) { rootFile.injectedRootInfo }
    }

    fun getInjectedFileInfo(file: VirtualFile): ParadoxFileInfo? {
        return with(dataService) { file.injectedFileInfo }
    }

    fun getInjectedLocaleConfig(file: VirtualFile): CwtLocaleConfig? {
        return with(dataService) { file.injectedLocaleConfig }
    }

    fun getInjectedRootKeys(file: VirtualFile): List<String> {
        return with(dataService) { file.injectedRootKeys.orEmpty() }
    }

    fun useDefaultFileExtensions(): Boolean {
        return dataService.useDefaultFileExtensions
    }

    fun useGameTypeInference(): Boolean {
        return dataService.useGameTypeInference
    }

    fun getMarkedRootInfo(): ParadoxRootInfo? {
        return dataService.markedRootInfo
    }

    fun getMarkedFileInfo(): ParadoxFileInfo? {
        return dataService.markedFileInfo
    }

    // endregion

    // region Manipulation Methods

    fun injectRootInfo(rootFile: VirtualFile, rootInfo: ParadoxRootInfo?): Boolean {
        with(dataService) { rootFile.injectedRootInfo = rootInfo }
        return true
    }

    fun injectFileInfo(file: VirtualFile, fileInfo: ParadoxFileInfo?): Boolean {
        with(dataService) { file.injectedFileInfo = fileInfo }
        return true
    }

    fun injectLocaleConfig(file: VirtualFile, localeConfig: CwtLocaleConfig?): Boolean {
        with(dataService) { file.injectedLocaleConfig = localeConfig }
        return true
    }

    fun injectRootKeys(file: VirtualFile, rootKeys: List<String>): Boolean {
        with(dataService) { file.injectedRootKeys = rootKeys.orNull() }
        return true
    }

    fun configureUseDefaultFileExtensions(value: Boolean) {
        dataService.useDefaultFileExtensions = value
    }

    fun configureUseGameTypeInference(value: Boolean) {
        dataService.useGameTypeInference = value
    }

    fun markRootInfo(rootInfo: ParadoxRootInfo) {
        dataService.markedRootInfo = rootInfo
    }

    fun clearMarkedRootInfo() {
        dataService.markedRootInfo = null
    }

    fun markFileInfo(fileInfo: ParadoxFileInfo) {
        dataService.markedFileInfo = fileInfo
    }

    fun markFileInfo(gameType: ParadoxGameType, path: String, entry: String = "", group: ParadoxFileGroup? = null) {
        val filePath = ParadoxPath.resolve(path)
        val fileEntry = entry
        val fileGroup = group ?: ParadoxFileGroup.resolvePossible(path.substringAfterLast('/'))
        val fileInfo = ParadoxFileInfo(filePath, fileEntry, fileGroup, ParadoxRootInfo.Injected(gameType))
        dataService.markedFileInfo = fileInfo
    }

    fun clearMarkedFileInfo() {
        dataService.markedFileInfo = null
    }

    // endregion
}
