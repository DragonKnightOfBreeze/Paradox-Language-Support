package icu.windea.pls.lang.analysis

import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.core.util.values.LazyValue
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxRootInfo
import java.nio.file.Path

interface ParadoxAnalysisScope {
    companion object : ParadoxAnalysisScope
}

// 3.0.1 optimize: make all analysis data accessors scoped and inline only

/** @see ParadoxAnalysisDataManager.useDefaultFileExtensions */
context(_: ParadoxAnalysisScope)
inline var useDefaultFileExtensions: Boolean // region by ParadoxAnalysisDataManager::useDefaultFileExtensions
    get() = ParadoxAnalysisDataManager.useDefaultFileExtensions
    set(value) = run { ParadoxAnalysisDataManager.useDefaultFileExtensions = value } // endregion
/** @see ParadoxAnalysisDataManager.useGameTypeInference */
context(_: ParadoxAnalysisScope)
inline var useGameTypeInference: Boolean // region by ParadoxAnalysisDataManager::useGameTypeInference
    get() = ParadoxAnalysisDataManager.useGameTypeInference
    set(value) = run { ParadoxAnalysisDataManager.useGameTypeInference = value } // endregion

/** @see ParadoxAnalysisDataManager.markedRootInfo */
context(_: ParadoxAnalysisScope)
inline var markedRootInfo: ParadoxRootInfo? // region by ParadoxAnalysisDataManager::markedRootInfo
    get() = ParadoxAnalysisDataManager.markedRootInfo
    set(value) = run { ParadoxAnalysisDataManager.markedRootInfo = value } // endregion
/** @see ParadoxAnalysisDataManager.markedFileInfo */
context(_: ParadoxAnalysisScope)
inline var markedFileInfo: ParadoxFileInfo? // region by ParadoxAnalysisDataManager::markedFileInfo
    get() = ParadoxAnalysisDataManager.markedFileInfo
    set(value) = run { ParadoxAnalysisDataManager.markedFileInfo = value } // endregion
/** @see ParadoxAnalysisDataManager.markedRootPath */
context(_: ParadoxAnalysisScope)
inline var markedRootPath: String? // region by ParadoxAnalysisDataManager::markedRootPath
    get() = ParadoxAnalysisDataManager.markedRootPath
    set(value) = run { ParadoxAnalysisDataManager.markedRootPath = value } // endregion
/** @see ParadoxAnalysisDataManager.markedRootDirectory */
context(_: ParadoxAnalysisScope)
inline var markedRootDirectory: Path? // region by ParadoxAnalysisDataManager::markedRootDirectory
    get() = ParadoxAnalysisDataManager.markedRootDirectory
    set(value) = run { ParadoxAnalysisDataManager.markedRootDirectory = value } // endregion
/** @see ParadoxAnalysisDataManager.markedConfigPath */
context(_: ParadoxAnalysisScope)
inline var markedConfigPath: String? // region by ParadoxAnalysisDataManager::markedConfigPath
    get() = ParadoxAnalysisDataManager.markedConfigPath
    set(value) = run { ParadoxAnalysisDataManager.markedConfigPath = value } // endregion
/** @see ParadoxAnalysisDataManager.markedConfigDirectory */
context(_: ParadoxAnalysisScope)
inline var markedConfigDirectory: Path? // region by ParadoxAnalysisDataManager::markedConfigDirectory
    get() = ParadoxAnalysisDataManager.markedConfigDirectory
    set(value) = run { ParadoxAnalysisDataManager.markedConfigDirectory = value } // endregion

/** @see ParadoxAnalysisDataManager.useOnlyBuiltInAndInjectedConfigFiles */
context(_: ParadoxAnalysisScope)
inline var useOnlyBuiltInAndInjectedConfigFiles: Boolean  // region by ParadoxAnalysisDataManager::useOnlyBuiltInAndInjectedConfigFiles
    get() = ParadoxAnalysisDataManager.useOnlyBuiltInAndInjectedConfigFiles
    set(value) = run { ParadoxAnalysisDataManager.useOnlyBuiltInAndInjectedConfigFiles = value } // endregion
/** @see ParadoxAnalysisDataManager.useOnlyInjectedConfigFiles */
context(_: ParadoxAnalysisScope)
inline var useOnlyInjectedConfigFiles: Boolean // region by ParadoxAnalysisDataManager::useOnlyInjectedConfigFiles
    get() = ParadoxAnalysisDataManager.useOnlyInjectedConfigFiles
    set(value) = run { ParadoxAnalysisDataManager.useOnlyInjectedConfigFiles = value } // endregion

/** @see ParadoxAnalysisDataManager.Keys.cachedRootInfo */
context(_: ParadoxAnalysisScope)
inline var VirtualFile.cachedRootInfo: LazyValue<ParadoxRootInfo>? // region by ParadoxAnalysisDataManager.Keys.cachedRootInfo
    get() = ParadoxAnalysisDataManager.getData(this, ParadoxAnalysisDataManager.Keys.cachedRootInfo)
    set(value) = ParadoxAnalysisDataManager.setData(this, ParadoxAnalysisDataManager.Keys.cachedRootInfo, value) // endregion
/** @see ParadoxAnalysisDataManager.Keys.cachedFileInfo */
context(_: ParadoxAnalysisScope)
inline var VirtualFile.cachedFileInfo: LazyValue<ParadoxFileInfo>? // region by ParadoxAnalysisDataManager.Keys.cachedFileInfo
    get() = ParadoxAnalysisDataManager.getData(this, ParadoxAnalysisDataManager.Keys.cachedFileInfo)
    set(value) = ParadoxAnalysisDataManager.setData(this, ParadoxAnalysisDataManager.Keys.cachedFileInfo, value) // endregion
/** @see ParadoxAnalysisDataManager.Keys.cachedLocaleConfig */
context(_: ParadoxAnalysisScope)
inline var VirtualFile.cachedLocaleConfig: LazyValue<CwtLocaleConfig>? // region by ParadoxAnalysisDataManager.Keys.cachedLocaleConfig
    get() = ParadoxAnalysisDataManager.getData(this, ParadoxAnalysisDataManager.Keys.cachedLocaleConfig)
    set(value) = ParadoxAnalysisDataManager.setData(this, ParadoxAnalysisDataManager.Keys.cachedLocaleConfig, value) // endregion

/** @see ParadoxAnalysisDataManager.Keys.injectedRootInfo */
context(_: ParadoxAnalysisScope)
inline var VirtualFile.injectedRootInfo: ParadoxRootInfo? // region by ParadoxAnalysisDataManager.Keys.injectedRootInfo
    get() = ParadoxAnalysisDataManager.getData(this, ParadoxAnalysisDataManager.Keys.injectedRootInfo)
    set(value) = ParadoxAnalysisDataManager.setData(this, ParadoxAnalysisDataManager.Keys.injectedRootInfo, value) // endregion
/** @see ParadoxAnalysisDataManager.Keys.injectedFileInfo */
context(_: ParadoxAnalysisScope)
inline var VirtualFile.injectedFileInfo: ParadoxFileInfo? // region by ParadoxAnalysisDataManager.Keys.injectedFileInfo
    get() = ParadoxAnalysisDataManager.getData(this, ParadoxAnalysisDataManager.Keys.injectedFileInfo)
    set(value) = ParadoxAnalysisDataManager.setData(this, ParadoxAnalysisDataManager.Keys.injectedFileInfo, value) // endregion
/** @see ParadoxAnalysisDataManager.Keys.injectedLocaleConfig */
context(_: ParadoxAnalysisScope)
inline var VirtualFile.injectedLocaleConfig: CwtLocaleConfig? // region by ParadoxAnalysisDataManager.Keys.injectedLocaleConfig
    get() = ParadoxAnalysisDataManager.getData(this, ParadoxAnalysisDataManager.Keys.injectedLocaleConfig)
    set(value) = ParadoxAnalysisDataManager.setData(this, ParadoxAnalysisDataManager.Keys.injectedLocaleConfig, value) // endregion
/** @see ParadoxAnalysisDataManager.Keys.injectedRootKeys */
context(_: ParadoxAnalysisScope)
inline var VirtualFile.injectedRootKeys: List<String>? // region by ParadoxAnalysisDataManager.Keys.injectedRootKeys
    get() = ParadoxAnalysisDataManager.getData(this, ParadoxAnalysisDataManager.Keys.injectedRootKeys)
    set(value) = ParadoxAnalysisDataManager.setData(this, ParadoxAnalysisDataManager.Keys.injectedRootKeys, value) // endregion

/** @see ParadoxAnalysisDataManager.Keys.sliceInfos */
context(_: ParadoxAnalysisScope)
inline var VirtualFile.sliceInfos: MutableSet<String>? // region by ParadoxAnalysisDataManager.Keys.sliceInfos
    get() = ParadoxAnalysisDataManager.getData(this, ParadoxAnalysisDataManager.Keys.sliceInfos)
    set(value) = ParadoxAnalysisDataManager.setData(this, ParadoxAnalysisDataManager.Keys.sliceInfos, value) // endregion
