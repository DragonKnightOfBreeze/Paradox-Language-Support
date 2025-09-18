package icu.windea.pls.ep.tools.model

/**
 * 模组导入结果（平台无关）。
 *
 * @property gameId 游戏类型ID，如 `stellaris`、`vic3`（某些来源可能为空）
 * @property collectionName 集合名称，如启动器的播放列表名（某些来源可能为空或固定值）
 * @property mods 导入到的模组信息列表
 */
 data class ParadoxModImportData(
     val gameId: String? = null,
     val collectionName: String? = null,
     val mods: List<ParadoxModInfo> = emptyList(),
 )
