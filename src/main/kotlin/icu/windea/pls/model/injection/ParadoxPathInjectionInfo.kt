package icu.windea.pls.model.injection

import icu.windea.pls.model.*

/**
 * 路径的语言注入信息。
 *
 * @property gameType 游戏类型。
 * @property path 相对于游戏或模组目录的路径。
 */
class ParadoxPathInjectionInfo(
    val gameType: ParadoxGameType,
    val path: String
)
