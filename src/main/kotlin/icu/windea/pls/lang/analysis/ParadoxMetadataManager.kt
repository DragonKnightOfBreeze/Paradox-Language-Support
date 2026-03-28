package icu.windea.pls.lang.analysis

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.metadata.ParadoxDescriptorModInfo
import icu.windea.pls.model.metadata.ParadoxLauncherSettingsJsonInfo
import icu.windea.pls.model.metadata.ParadoxMetadataJsonInfo

object ParadoxMetadataManager {
    private val logger = thisLogger()

    val gameTypesUseMetadataJson: List<ParadoxGameType> = listOf(ParadoxGameType.Vic3, ParadoxGameType.Eu5)
    val gameTypesUseDescriptorMod: List<ParadoxGameType> = buildList {
        addAll(ParadoxGameType.getAll())
        removeAll(gameTypesUseMetadataJson)
    }

    /**
     * 得到使用 `.metadata/metadata.json` 作为模组描述符的所有游戏类型。
     *
     * @param withCore 是否包含通用游戏类型（[ParadoxGameType.Core]）。
     */
    fun useMetadataJson(gameType: ParadoxGameType, withCore: Boolean = false): Boolean {
        if (withCore && gameType == ParadoxGameType.Core) return true
        return gameType in gameTypesUseMetadataJson
    }

    /**
     * 得到使用 `descriptor.mod` 作为模组描述符的所有游戏类型。
     *
     * @param withCore 是否包含通用游戏类型（[ParadoxGameType.Core]）。
     */
    fun useDescriptorMod(gameType: ParadoxGameType, withCore: Boolean = false): Boolean {
        if (withCore && gameType == ParadoxGameType.Core) return true
        return gameType in gameTypesUseDescriptorMod
    }

    fun isLauncherSettingsJsonFile(file: VirtualFile): Boolean {
        return ParadoxMetadataService.isLauncherSettingsJsonFile(file)
    }

    fun getLauncherSettingsJsonFile(rootFile: VirtualFile): VirtualFile? {
        return runSmartReadAction { ParadoxMetadataService.getLauncherSettingsJsonFile(rootFile) }
    }

    fun getLauncherSettingsJsonInfo(file: VirtualFile): ParadoxLauncherSettingsJsonInfo? {
        return runCatchingCancelable {
            runSmartReadAction { ParadoxMetadataService.resolveLauncherSettingsJsonInfo(file) }
        }.onFailure { logger.warn(it) }.getOrNull()
    }

    fun isMetadataJsonFile(file: VirtualFile): Boolean {
        return ParadoxMetadataService.isMetadataJsonFile(file)
    }

    fun getMetadataJsonFile(rootFile: VirtualFile): VirtualFile? {
        return runSmartReadAction { ParadoxMetadataService.getMetadataJsonFile(rootFile) }
    }

    fun getMetadataJsonInfo(file: VirtualFile): ParadoxMetadataJsonInfo? {
        return runCatchingCancelable {
            runSmartReadAction { ParadoxMetadataService.resolveMetadataJsonInfo(file) }
        }.onFailure { logger.warn(it) }.getOrNull()
    }

    @Suppress("unused")
    fun isDescriptorModFile(file: VirtualFile): Boolean {
        return ParadoxMetadataService.isDescriptorModFile(file)
    }

    fun getDescriptorModFile(rootFile: VirtualFile): VirtualFile? {
        return runSmartReadAction { ParadoxMetadataService.getDescriptorModFile(rootFile) }
    }

    fun getDescriptorModInfo(file: VirtualFile): ParadoxDescriptorModInfo? {
        return runCatchingCancelable {
            runSmartReadAction { ParadoxMetadataService.resolveDescriptorModInfo(file) }
        }.onFailure { logger.warn(it) }.getOrNull()
    }
}
