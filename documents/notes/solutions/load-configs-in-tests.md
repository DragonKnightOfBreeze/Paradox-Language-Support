# 需要在单元测试（集成测试）中加载并获取规则数据

> - 模块：测试

按需加载指定规则分组的数据。

```kotlin
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
```
