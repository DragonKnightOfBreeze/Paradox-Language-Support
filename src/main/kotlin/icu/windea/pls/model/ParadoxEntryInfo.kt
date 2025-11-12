package icu.windea.pls.model

import icu.windea.pls.core.optimized

/**
 * 入口信息。
 *
 * 说明：
 * - 入口的名称即入口目录相对于游戏或模组目录的路径。
 * - 入口分为主要入口和次要入口。主要入口也可能存在多个，其名称默认为空字符串。
 * - 游戏与模组文件实际上需要位于入口目录中，而非游戏或模组目录中。
 * - （PLS 认为）主要入口目录中的文件不能引用次要入口目录中的文件中的内容。
 * - 游戏与模组文件的（相对）路径，一般指相对于入口目录的路径。
 * - 对于游戏来说。主要入口的名称一般为空字符串（等同于游戏根目录）或 `game`（等同于游戏根目录下的 `game` 子目录）。
 * - 对于模组来说，主要入口的名称一般为空字符串（等同于模组根目录）。
 *
 * @property gameMain 游戏的主要入口。
 * @property gameExtra 游戏的额外入口。
 * @property modMain 模组的主要入口。
 * @property modExtra 模组的额外入口。
 */
data class ParadoxEntryInfo(
    val gameMain: Set<String> = emptySet(),
    val gameExtra: Set<String> = emptySet(),
    val modMain: Set<String> = emptySet(),
    val modExtra: Set<String> = emptySet(),
) {
    val gameEntryMap = (gameMain.ifEmpty { setOf("") } + gameExtra).toEntryMap()
    val modEntryMap = (modMain.ifEmpty { setOf("") } + modExtra).toEntryMap()

    private fun Set<String>.toEntryMap() = sortedDescending().associateWith { it.splitEntry() }.toMap().optimized()

    private fun String.splitEntry() = if (isEmpty()) emptyList() else split('/')
}
