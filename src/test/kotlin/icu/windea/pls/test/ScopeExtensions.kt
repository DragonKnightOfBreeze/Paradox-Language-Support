@file:Suppress("unused")

package icu.windea.pls.test

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.UsefulTestCase
import icu.windea.pls.config.configGroup.CwtConfigGroupService
import icu.windea.pls.core.toPath
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.lang.analysis.ParadoxAnalysisInjector
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Path

/** @see com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess.allowedRoots */
context(_: UsefulTestCase)
fun addAdditionalAllowedRoots(vararg roots: String?) {
    val additionalAllowedRoots = roots.mapNotNull { it?.toPath()?.toAbsolutePath()?.normalize()?.toString() }
    doAddAddtionalAllowedRoots(additionalAllowedRoots)
}

/** @see com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess.allowedRoots */
context(_: UsefulTestCase)
fun addAdditionalAllowedRoots(vararg roots: Path?) {
    val additionalAllowedRoots = roots.mapNotNull { it?.toAbsolutePath()?.normalize()?.toString() }
    doAddAddtionalAllowedRoots(additionalAllowedRoots)
}

private fun doAddAddtionalAllowedRoots(additionalAllowedRoots: List<String>) {
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
        configGroupService.refreshBuiltInConfigFiles(project)
        configGroupService.init(configGroups, project)
    }
}

context(_: UsefulTestCase)
fun markIntegrationTest() {
    ParadoxAnalysisInjector.configureUseDefaultFileExtensions(true)
    ParadoxAnalysisInjector.configureUseGameTypeInference(true)
}

context(_: UsefulTestCase)
fun clearIntegrationTest() {
    ParadoxAnalysisInjector.configureUseDefaultFileExtensions(false)
    ParadoxAnalysisInjector.configureUseGameTypeInference(false)
    ParadoxAnalysisInjector.clearMarkedRootInfo()
    ParadoxAnalysisInjector.clearMarkedFileInfo()
    ParadoxAnalysisInjector.clearMarkedRootDirectory()
    ParadoxAnalysisInjector.clearMarkedConfigDirectory()
}

context(_: UsefulTestCase)
fun markRootDirectory(relPath: String) {
    val testDataPath = "src/test/testData".toPathOrNull() ?: return
    val path = testDataPath.resolve(relPath)
    ParadoxAnalysisInjector.markRootDirectory(relPath, path)
}

context(_: UsefulTestCase)
fun markConfigDirectory(relPath: String) {
    val testDataPath = "src/test/testData".toPathOrNull() ?: return
    val path = testDataPath.resolve(relPath)
    ParadoxAnalysisInjector.markConfigDirectory(relPath, path)
}

context(_: UsefulTestCase)
fun markFileInfo(gameType: ParadoxGameType, path: String, entry: String = "", group: ParadoxFileGroup? = null) {
    ParadoxAnalysisInjector.markFileInfo(gameType, path, entry, group)
}

context(_: UsefulTestCase)
fun VirtualFile.injectFileInfo(gameType: ParadoxGameType, path: String, entry: String = "", group: ParadoxFileGroup? = null) {
    ParadoxAnalysisInjector.injectFileInfo(this, gameType, path, entry, group)
}
