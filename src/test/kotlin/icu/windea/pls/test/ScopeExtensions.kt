@file:Suppress("unused")

package icu.windea.pls.test

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.UsefulTestCase
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.CwtConfigGroupService
import icu.windea.pls.lang.analysis.ParadoxAnalysisInjector
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.runBlocking

context(_: UsefulTestCase)
fun initConfigGroup(project: Project, gameType: ParadoxGameType): CwtConfigGroup {
    val configGroupService = CwtConfigGroupService.getInstance(project)
    val groups = configGroupService.getConfigGroups().values
        .filter { it.gameType == ParadoxGameType.Core || it.gameType == gameType }
    runBlocking { configGroupService.init(groups, project) }
    return configGroupService.getConfigGroup(gameType)
}

context(_: UsefulTestCase)
fun initConfigGroups(project: Project, vararg gameTypes: ParadoxGameType) {
    val servconfigGroupService = CwtConfigGroupService.getInstance(project)
    val configGroups = servconfigGroupService.getConfigGroups().values
        .filter { it.gameType == ParadoxGameType.Core || (gameTypes.isEmpty() || it.gameType in gameTypes) }
    runBlocking { servconfigGroupService.init(configGroups, project) }
}

context(_: UsefulTestCase)
fun markIntegrationTest() {
    ParadoxAnalysisInjector.configureUseDefaultFileExtensions(true)
    ParadoxAnalysisInjector.configureUseGameTypeInference(true)
}

context(_: UsefulTestCase)
fun clearIntegrationTest() {
    ParadoxAnalysisInjector.clearMarkedFileInfo()
}

context(_: UsefulTestCase)
fun markFileInfo(gameType: ParadoxGameType, path: String, entry: String = "", group: ParadoxFileGroup? = null) {
    ParadoxAnalysisInjector.markFileInfo(gameType, path, entry, group)
}

context(_: UsefulTestCase)
fun VirtualFile.injectFileInfo(gameType: ParadoxGameType, path: String, entry: String = "", group: ParadoxFileGroup? = null) {
    ParadoxAnalysisInjector.injectFileInfo(this, gameType, path, entry, group)
}
