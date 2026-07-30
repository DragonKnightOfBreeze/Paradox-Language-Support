package icu.windea.pls.test

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.CwtConfigGroupFileSource
import icu.windea.pls.config.configGroup.CwtConfigGroupService
import icu.windea.pls.ep.config.configGroup.CwtConfigGroupFileProvider
import icu.windea.pls.lang.analysis.ParadoxAnalysisInjectionManager
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.runBlocking

object ChronicleTestManager {
    private val logger = thisLogger()
    private val refreshedConfigDirectories = mutableSetOf<VirtualFile>()
    @Volatile private var lastUseBuiltInGroupGroups = false
    @Volatile private var lastMarkedConfigPath: String? = null

    fun initConfigGroups(project: Project, gameTypes: Collection<ParadoxGameType>, onlyInjected: Boolean) {
        if (project.isDisposed) return
        val configGroups = getConfigGroups(project, gameTypes)
        if (!shouldRefreshConfigGroups(onlyInjected)) return // skip unnecessary reinitialization
        clearConfigGroups(configGroups) // clear config groups to be reinitialized
        if (project.isDisposed) return
        refreshConfigFiles(project, onlyInjected) // in case
        if (project.isDisposed) return
        initConfigGroups(project, configGroups, onlyInjected)
    }

    private fun getConfigGroups(project: Project, gameTypes: Collection<ParadoxGameType>): List<CwtConfigGroup> {
        return CwtConfigGroupService.getInstance(project).getConfigGroups().values
            .filter { it.gameType == ParadoxGameType.Core || (gameTypes.isEmpty() || it.gameType in gameTypes) }
    }

    private fun shouldRefreshConfigGroups(onlyInjected: Boolean): Boolean {
        if (lastUseBuiltInGroupGroups && lastMarkedConfigPath == ParadoxAnalysisInjectionManager.getMarkedConfigPath()) return false
        lastUseBuiltInGroupGroups = !onlyInjected
        lastMarkedConfigPath = ParadoxAnalysisInjectionManager.getMarkedConfigPath()
        return true
    }

    private fun clearConfigGroups(configGroups: List<CwtConfigGroup>) {
        configGroups.forEach { configGroup -> configGroup.clear() }
    }

    private fun refreshConfigFiles(project: Project, onlyInjected: Boolean) {
        if (!onlyInjected) refreshBuiltInFiles(project)
        refreshInjectedFiles(project)
    }

    private fun refreshBuiltInFiles(project: Project) {
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

    private fun refreshInjectedFiles(project: Project) {
        val files = CwtConfigGroupFileProvider.EP_NAME.extensionList
            .filter { it.source == CwtConfigGroupFileSource.Injected }
            .mapNotNullTo(mutableSetOf()) { it.getRootDirectory(project) }
        if (files.isEmpty()) return
        files.removeAll(refreshedConfigDirectories)
        if (files.isEmpty()) return
        logger.info("Prepare to refresh injected config directories...")
        files.forEach {
            VfsUtil.markDirtyAndRefresh(false, true, true, it)
            logger.info("Refreshed builtin injected directory: ${it.presentableUrl}")
        }
        refreshedConfigDirectories.addAll(files)
    }

    private fun initConfigGroups(project: Project, configGroups: List<CwtConfigGroup>, onlyInjected: Boolean) {
        if (onlyInjected) {
            logger.info("Prepare to init injected config groups...")
        } else {
            logger.info("Prepare to init config groups...")
        }
        runBlocking { CwtConfigGroupService.getInstance(project).initConfigGroups(configGroups) }
    }
}
