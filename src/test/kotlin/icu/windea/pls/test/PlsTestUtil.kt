package icu.windea.pls.test

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.CwtConfigGroupService
import icu.windea.pls.lang.PlsKeys
import icu.windea.pls.model.ParadoxFileInfo
import icu.windea.pls.model.ParadoxFileType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.paths.ParadoxPath
import kotlinx.coroutines.runBlocking

object PlsTestUtil {
    fun initConfigGroup(project: Project, gameType: ParadoxGameType): CwtConfigGroup {
        val configGroupService = CwtConfigGroupService.getInstance(project)
        val groups = configGroupService.getConfigGroups().values
        runBlocking { configGroupService.init(groups, project) }
        return configGroupService.getConfigGroup(gameType)
    }

    fun initConfigGroups(project: Project) {
        val configGroupService = CwtConfigGroupService.getInstance(project)
        val configGroups = configGroupService.getConfigGroups().values
        runBlocking { configGroupService.init(configGroups, project) }
    }

    fun initConfigGroups(project: Project, vararg gameTypes: ParadoxGameType) {
        val configGroupService = CwtConfigGroupService.getInstance(project)
        val configGroups = configGroupService.getConfigGroups().values
            .filter { it.gameType == ParadoxGameType.Core || it.gameType in gameTypes }
        runBlocking { configGroupService.init(configGroups, project) }
    }

    fun injectFileInfo(file: VirtualFile, path: String, gameType: ParadoxGameType) {
        val fileType = ParadoxFileType.resolvePossible(file.name)
        val fileInfo = ParadoxFileInfo(ParadoxPath.resolve(path), "", fileType, ParadoxRootInfo.Injected(gameType))
        file.putUserData(PlsKeys.injectedFileInfo, fileInfo)
        file.putUserData(PlsKeys.injectedGameType, gameType)
    }
}
