package icu.windea.pls.model.analysis

import com.google.common.collect.ImmutableSet
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
 * - 这里列出的是相对于游戏根目录，去除扩展名（如 `.exe`）后的文件名。
 *
 * @see ParadoxGameType
 */
data class ParadoxGameTypeMetadata(
    val gameType: ParadoxGameType,
    val gameMainEntries: Set<String>,
    val gameExtraEntries: Set<String>,
    val modMainEntries: Set<String>,
    val modExtraEntries: Set<String>,
    val executableBaseNames: Set<String>,
) {
    val gameEntries: Set<String> = computeGameEntries()
    val modEntries: Set<String> = computeModEntries()

    val gameEntryPaths: Set<ParadoxPath> = computeEntryPaths(gameEntries)
    val modEntryPaths: Set<ParadoxPath> = computeEntryPaths(modEntries)

    private fun computeGameEntries(): Set<String> {
        return ImmutableSet.builder<String>()
            .addAll(gameMainEntries).addAll(gameExtraEntries)
            .build()
    }

    private fun computeModEntries(): Set<String> {
        return ImmutableSet.builder<String>()
            .addAll(modMainEntries).addAll(modExtraEntries)
            .build()
    }

    private fun computeEntryPaths(entries: Set<String>): Set<ParadoxPath> {
        return ImmutableSet.builder<ParadoxPath>()
            .apply { entries.sortedDescending().forEach { add(ParadoxPath.resolve(it)) } }
            .build()
    }
}
