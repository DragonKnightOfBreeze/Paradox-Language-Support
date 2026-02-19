package icu.windea.pls.model

import icu.windea.pls.lang.util.ParadoxDefineManager

/**
 * 定值的解析信息。
 *
 * @property namespace 命名空间。
 * @property variable 变量名。
 * @property gameType 游戏类型。
 */
data class ParadoxDefineInfo(
    val namespace: String,
    val variable: String?,
    val gameType: ParadoxGameType,
) {
    val expression: String get() = ParadoxDefineManager.getExpression(namespace, variable)
}
