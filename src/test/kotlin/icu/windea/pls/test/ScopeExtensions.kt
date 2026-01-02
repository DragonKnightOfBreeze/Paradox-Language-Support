package icu.windea.pls.test

import com.intellij.openapi.project.Project
import com.intellij.testFramework.UsefulTestCase
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.CwtConfigGroupService
import icu.windea.pls.lang.analyze.ParadoxAnalyzeInjector
import icu.windea.pls.model.ParadoxFileGroup
import icu.windea.pls.model.ParadoxGameType
import kotlinx.coroutines.runBlocking

context(_: UsefulTestCase)
fun initConfigGroup(project: Project, gameType: ParadoxGameType): CwtConfigGroup {
    val configGroupService = CwtConfigGroupService.getInstance(project)
    val groups = configGroupService.getConfigGroups().values
    runBlocking { configGroupService.init(groups, project) }
    return configGroupService.getConfigGroup(gameType)
}

context(_: UsefulTestCase)
fun initConfigGroups(project: Project) {
    val configGroupService = CwtConfigGroupService.getInstance(project)
    val configGroups = configGroupService.getConfigGroups().values
    runBlocking { configGroupService.init(configGroups, project) }
}

context(_: UsefulTestCase)
fun initConfigGroups(project: Project, vararg gameTypes: ParadoxGameType) {
    val configGroupService = CwtConfigGroupService.getInstance(project)
    val configGroups = configGroupService.getConfigGroups().values
        .filter { it.gameType == ParadoxGameType.Core || it.gameType in gameTypes }
    runBlocking { configGroupService.init(configGroups, project) }
}

context(_: UsefulTestCase)
fun markIntegrationTest() {
    ParadoxAnalyzeInjector.configureUseDefaultFileExtensions(true)
    ParadoxAnalyzeInjector.configureUseGameTypeInference(true)
}

context(_: UsefulTestCase)
fun markFileInfo(gameType: ParadoxGameType, path: String, entry: String = "", group: ParadoxFileGroup? = null) {
    ParadoxAnalyzeInjector.markFileInfo(gameType, path, entry, group)
}
