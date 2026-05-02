package icu.windea.pls.lang.analysis

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.model.analysis.ParadoxDescriptorModInfo
import icu.windea.pls.model.analysis.ParadoxLauncherSettingsJsonInfo
import icu.windea.pls.model.analysis.ParadoxMetadataJsonInfo

object ParadoxMetadataManager {
    private val logger = thisLogger()


    fun getLauncherSettingsJsonFile(rootFile: VirtualFile): VirtualFile? {
        return runSmartReadAction { ParadoxMetadataService.getLauncherSettingsJsonFile(rootFile) }
    }

    fun getLauncherSettingsJsonInfo(file: VirtualFile): ParadoxLauncherSettingsJsonInfo? {
        return runCatchingCancelable {
            runSmartReadAction { ParadoxMetadataService.resolveLauncherSettingsJsonInfo(file) }
        }.onFailure { logger.warn(it) }.getOrNull()
    }

    fun getMetadataJsonFile(rootFile: VirtualFile): VirtualFile? {
        return runSmartReadAction { ParadoxMetadataService.getMetadataJsonFile(rootFile) }
    }

    fun getMetadataJsonInfo(file: VirtualFile): ParadoxMetadataJsonInfo? {
        return runCatchingCancelable {
            runSmartReadAction { ParadoxMetadataService.resolveMetadataJsonInfo(file) }
        }.onFailure { logger.warn(it) }.getOrNull()
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
