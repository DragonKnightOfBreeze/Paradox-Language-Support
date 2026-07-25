package icu.windea.pls.test

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.config.configGroup.CwtConfigGroupFileSource
import icu.windea.pls.config.configGroup.CwtConfigGroupService
import icu.windea.pls.ep.config.configGroup.CwtConfigGroupFileProvider
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.runBlocking

object ChronicleTestManager {
    private val logger = thisLogger()
    private val refreshedConfigDirectories = mutableSetOf<VirtualFile>()

    fun initConfigGroups(project: Project, vararg gameTypes: ParadoxGameType) {
        if (project.isDisposed) return
        val configGroupService = CwtConfigGroupService.getInstance(project)
        val configGroups = configGroupService.getConfigGroups().values
            .filter { it.gameType == ParadoxGameType.Core || (gameTypes.isEmpty() || it.gameType in gameTypes) }
        if (project.isDisposed) return
        refreshBuiltInAndInjectedConfigFiles(project) // in case
        if (project.isDisposed) return
        logger.info("Prepare to init config groups...")
        runBlocking { configGroupService.initConfigGroups(configGroups) }
    }

    fun initInjectedConfigGroups(project: Project, vararg gameTypes: ParadoxGameType) {
        if (project.isDisposed) return
        val configGroupService = CwtConfigGroupService.getInstance(project)
        val configGroups = configGroupService.getConfigGroups().values
            .filter { it.gameType == ParadoxGameType.Core || (gameTypes.isEmpty() || it.gameType in gameTypes) }
        if (project.isDisposed) return
        refreshInjectedConfigFiles(project) // in case
        if (project.isDisposed) return
        logger.info("Prepare to init injected config groups...")
        runBlocking { configGroupService.initConfigGroups(configGroups) }
    }

    private fun refreshBuiltInAndInjectedConfigFiles(project: Project) {
        refreshBuiltInConfigFilesIfNeeded(project)
        refreshInjectedConfigFilesIfNeeded(project)
    }

    private fun refreshInjectedConfigFiles(project: Project) {
        refreshInjectedConfigFilesIfNeeded(project)
    }

    private fun refreshBuiltInConfigFilesIfNeeded(project: Project) {
        val files = CwtConfigGroupFileProvider.EP_NAME.extensionList
            .filter { it.source == CwtConfigGroupFileSource.BuiltIn }
            .mapNotNullTo(mutableSetOf()) { it.getRootDirectory(project) }
        if (files.isEmpty()) return
        files.removeAll(refreshedConfigDirectories)
        if (files.isEmpty()) return
        logger.info("Prepare to refresh builtin config directories...")
        files.forEach {
            VfsUtil.markDirtyAndRefresh(false, true, true, it)
            logger.info("Refreshed builtin config directory: ${it.presentableUrl}")
        }
        refreshedConfigDirectories.addAll(files)
    }

    private fun refreshInjectedConfigFilesIfNeeded(project: Project) {
        val files = CwtConfigGroupFileProvider.EP_NAME.extensionList
            .filter { it.source == CwtConfigGroupFileSource.Injected }
            .mapNotNullTo(mutableSetOf()) { it.getRootDirectory(project) }
        if (files.isEmpty()) return
        files.removeAll(refreshedConfigDirectories)
        if (files.isEmpty()) return
        logger.info("Prepare to refresh injected config directories...")
        files.forEach {
            VfsUtil.markDirtyAndRefresh(false, true, true, it)
            logger.info("Refreshed injected config directory: ${it.presentableUrl}")
        }
        refreshedConfigDirectories.addAll(files)
    }
}
