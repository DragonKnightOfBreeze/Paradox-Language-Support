package icu.windea.pls.model.injection

import icu.windea.pls.model.ParadoxGameType

/**
 * 路径的语言注入信息。
 *
 * @property gameType 游戏类型。
 * @property path 相对于入口目录的路径。
 */
class ParadoxPathInjectionInfo(
    val gameType: ParadoxGameType,
    val path: String
)
