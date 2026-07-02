package icu.windea.pls.model.analysis

import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.paths.ParadoxPath

/**
 * 游戏类型的额外的元数据。
 *
 * 读取并使用来自 `/data/game_type_metadata_list.json5` 的 JSON 数据。
 *
 * **关于游戏或模组的入口：**
 *
 * - 入口名称即入口目录相对于入口目录的路径。可以使用 `*` 匹配任意子路径。
 * - 入口分为主要入口和次要入口。主要入口也可能存在多个，其名称默认为空字符串。
 * - 游戏与模组文件实际上需要位于入口目录中，而非游戏或模组目录中。
 * - （插件认为）主要入口目录中的文件不能引用次要入口目录中的文件中的内容。
 * - 游戏与模组文件的（相对）路径，一般指相对于入口目录的路径。
 * - 对于游戏来说。主要入口名称一般为空字符串（等同于游戏根目录）或 `game`（等同于游戏根目录下的 `game` 子目录）。
 * - 对于模组来说，主要入口名称一般为空字符串（等同于模组根目录）。
 *
 * **关于游戏的可执行文件：**
 *
 * - 这里列出的是相对于游戏根目录，去除扩展名（如 `.exe`）后的路径。
 *
 * @see ParadoxGameType
 */
data class ParadoxGameTypeMetadata(
    val gameType: ParadoxGameType,
    val gameMainEntries: Set<String>,
    val gameExtraEntries: Set<String>,
    val modMainEntries: Set<String>,
    val modExtraEntries: Set<String>,
    val executablePaths: Set<String>,
) {
    val gameEntries: Set<String> = gameMainEntries + gameExtraEntries
    val modEntries: Set<String> = modMainEntries + modExtraEntries

    val gameEntryPaths: Set<ParadoxPath> = gameEntries.toEntryPaths()
    val modEntryPaths: Set<ParadoxPath> = modEntries.toEntryPaths()

    private fun Set<String>.toEntryPaths() = sortedDescending().mapTo(mutableSetOf()) { ParadoxPath.resolve(it) }
}
