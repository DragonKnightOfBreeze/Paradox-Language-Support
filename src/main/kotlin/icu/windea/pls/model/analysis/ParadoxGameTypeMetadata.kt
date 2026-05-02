package icu.windea.pls.model.analysis

import icu.windea.pls.model.ParadoxGameType

/**
 * 游戏类型的额外的元数据。
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
interface ParadoxGameTypeMetadata {
    val gameType: ParadoxGameType

    val gameMainEntries: Set<String>
    val gameExtraEntries: Set<String>
    val gameEntryMap: Map<String, Set<String>>
    val modMainEntries: Set<String>
    val modExtraEntries: Set<String>
    val modEntryMap: Map<String, Set<String>>

    val executablePaths: Set<String>
}
