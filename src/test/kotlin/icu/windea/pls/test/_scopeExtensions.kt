@file:Suppress("unused")

package icu.windea.pls.test

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.UsefulTestCase
import icu.windea.pls.config.configGroup.CwtConfigGroupService
import icu.windea.pls.core.toPath
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.lang.analysis.ParadoxAnalysisInjectionManager
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.ParadoxRootInfo
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Path

/** @see com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess.allowedRoots */
context(_: UsefulTestCase)
fun addAdditionalAllowedRoots(vararg roots: String?) {
    val additionalAllowedRoots = roots.mapNotNull { it?.toPath()?.toAbsolutePath()?.normalize()?.toString() }
    doAddAdditionalAllowedRoots(additionalAllowedRoots)
}

/** @see com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess.allowedRoots */
context(_: UsefulTestCase)
fun addAdditionalAllowedRoots(vararg roots: Path?) {
    val additionalAllowedRoots = roots.mapNotNull { it?.toAbsolutePath()?.normalize()?.toString() }
    doAddAdditionalAllowedRoots(additionalAllowedRoots)
}

private fun doAddAdditionalAllowedRoots(additionalAllowedRoots: List<String>) {
    val oldValue = System.getProperty("vfs.additional-allowed-roots").orEmpty()
    val newList = listOf(oldValue).filter { it.isNotBlank() } + additionalAllowedRoots
    val newValue = newList.distinct().joinToString(File.pathSeparator)
    System.setProperty("vfs.additional-allowed-roots", newValue)
}

context(_: UsefulTestCase)
fun initConfigGroups(project: Project, vararg gameTypes: ParadoxGameType) {
    val configGroupService = CwtConfigGroupService.getInstance(project)
    val configGroups = configGroupService.getConfigGroups().values
        .filter { it.gameType == ParadoxGameType.Core || (gameTypes.isEmpty() || it.gameType in gameTypes) }
    runBlocking {
        configGroupService.refreshBuiltInConfigFiles()
        configGroupService.initConfigGroups(configGroups)
    }
}

context(_: UsefulTestCase)
fun markIntegrationTest() {
    ParadoxAnalysisInjectionManager.configureUseDefaultFileExtensions(true)
    ParadoxAnalysisInjectionManager.configureUseGameTypeInference(true)

    addAdditionalAllowedRoots(PathManager.getPluginsDir()) // Why should I add this? So unreasonable.
}

context(_: UsefulTestCase)
fun clearIntegrationTest() {
    ParadoxAnalysisInjectionManager.configureUseDefaultFileExtensions(false)
    ParadoxAnalysisInjectionManager.configureUseGameTypeInference(false)
    ParadoxAnalysisInjectionManager.clearMarkedRootInfo()
    ParadoxAnalysisInjectionManager.clearMarkedFileInfo()
    ParadoxAnalysisInjectionManager.clearMarkedRootDirectory()
    ParadoxAnalysisInjectionManager.clearMarkedConfigDirectory()
}

context(_: UsefulTestCase)
fun markRootDirectory(relPath: String) {
    val testDataPath = "src/test/testData".toPathOrNull() ?: return
    val path = testDataPath.resolve(relPath)
    ParadoxAnalysisInjectionManager.markRootDirectory(relPath, path)
}

context(_: UsefulTestCase)
fun markConfigDirectory(relPath: String) {
    val testDataPath = "src/test/testData".toPathOrNull() ?: return
    val path = testDataPath.resolve(relPath)
    ParadoxAnalysisInjectionManager.markConfigDirectory(relPath, path)
}

context(_: UsefulTestCase)
fun createRootInfo(gameType: ParadoxGameType, gameVersion: String? = null): ParadoxRootInfo.Injected {
    return ParadoxAnalysisInjectionManager.createRootInfo(gameType, gameVersion)
}

context(_: UsefulTestCase)
fun markFileInfo(gameType: ParadoxGameType, path: String, entry: String = "", group: ParadoxFileGroup? = null) {
    ParadoxAnalysisInjectionManager.markFileInfo(createRootInfo(gameType), path, entry, group)
}

context(_: UsefulTestCase)
fun markFileInfo(rootInfo: ParadoxRootInfo, path: String, entry: String = "", group: ParadoxFileGroup? = null) {
    ParadoxAnalysisInjectionManager.markFileInfo(rootInfo, path, entry, group)
}

context(_: UsefulTestCase)
fun VirtualFile.injectFileInfo(gameType: ParadoxGameType, path: String, entry: String = "", group: ParadoxFileGroup? = null) {
    ParadoxAnalysisInjectionManager.injectFileInfo(this, createRootInfo(gameType), path, entry, group)
}

context(_: UsefulTestCase)
fun VirtualFile.injectFileInfo(rootInfo: ParadoxRootInfo, path: String, entry: String = "", group: ParadoxFileGroup? = null) {
    ParadoxAnalysisInjectionManager.injectFileInfo(this, rootInfo, path, entry, group)
}
